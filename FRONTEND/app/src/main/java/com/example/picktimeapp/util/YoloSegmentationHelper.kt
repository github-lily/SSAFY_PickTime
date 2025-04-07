package com.example.picktimeapp.util

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import com.example.picktimeapp.data.model.YoloBox
import com.example.picktimeapp.data.model.YoloResult
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min
import org.opencv.core.Core

class YoloSegmentationHelper(private val context: Context) : Closeable {

    @Volatile
    private var isRunning = false

    @Volatile
    private var shouldRun = true

    // 모델 관련 상수
    private var interpreter: Interpreter? = null
    private val modelName = "ai/best_1280_736_float32.tflite"
    private val MODEL_WIDTH = 1280
    private val MODEL_HEIGHT = 736

    // 클래스 상수
    private val FRET_INDEX = 0
    private val NUT_INDEX = 1
    private val CLASS_COLORS = arrayOf(
        Scalar(0.0, 255.0, 0.0, 200.0),  // 초록색 (fret)
        Scalar(255.0, 0.0, 0.0, 200.0)   // 빨간색 (nut)
    )

    // 임계값 상수
    private val CONFIDENCE_THRESHOLD = 0.5f
    private val MASK_THRESHOLD = 0.5

    init {
        try {
            loadModel()
        } catch (e: Exception) {
            Log.e("✅YOLO", "모델 로딩 실패: ${e.message}")
        }
    }

    // 모델 불러오기
    private fun loadModel() {
        try {
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }

            val modelFile = FileUtil.loadMappedFile(context, modelName)
            interpreter = Interpreter(modelFile, options)

            // 입력과 출력 텐서 정보 확인
            val inputTensor = interpreter?.getInputTensor(0)
            val outputTensor = interpreter?.getOutputTensor(0)

            Log.d("✅YOLO", "모델 로드 성공: 입력 형태=${inputTensor?.shape()?.contentToString()}, 출력 형태=${outputTensor?.shape()?.contentToString()}")
            Log.d("✅YOLO", "입력 데이터 타입: ${inputTensor?.dataType()}, 출력 데이터 타입: ${outputTensor?.dataType()}")
        } catch (e: Exception) {
            Log.e(TAG, "모델 로딩 실패", e)
            throw e
        }
    }

    fun runInference(bitmap: Bitmap): YoloResult {
        val interpreter = this.interpreter ?: run {
            Log.e("✅YOLO", "❌ 인터프리터가 초기화되지 않음")
            return YoloResult(detections = emptyList(), bitmap = null)
        }

        // 이미 추론 중이면 중복 실행 방지
        synchronized(this) {
            if (isRunning) {
                Log.w("✅YOLO", "❗이미 추론 중이라 스킵")
                return YoloResult(detections = emptyList(), bitmap = null)
            }
            isRunning = true
        }

        try {
            // 원본 이미지 크기 기록
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height
//            Log.d("✅YOLO", "원본 이미지 크기: ${bitmap.width}(w) x ${bitmap.height}(h)")

            // 입력 텐서 준비
            val inputShape = interpreter.getInputTensor(0).shape()
            Log.d("✅YOLO", "모델 입력 이미지 크기: ${inputShape[2]}(w) x ${inputShape[1]}(h)")
            val inputBuffer = prepareInputBuffer(bitmap, inputShape)

            // 출력 준비
            val outputs = HashMap<Int, Any>()
            val output0 = Array(1) { Array(38) { FloatArray(19320) } }
            val output1 = Array(1) { Array(184) { Array(320) { FloatArray(32) } } }
            outputs[0] = output0
            outputs[1] = output1

            // 모델 추론 실행
            interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)
            Log.d("✅YOLO", "모델 추론 완료")

            // 마스크 데이터 추출
            val maskRaw = output1[0]  // [184][320][32]
            val maskHeight = maskRaw.size
            val maskWidth = maskRaw[0].size

            // 클래스별 마스크 추출
            val classMasks = Array(2) { classIdx ->
                Array(maskHeight) { y ->
                    FloatArray(maskWidth) { x ->
                        maskRaw[y][x][classIdx]
                    }
                }
            }

            // 박스 탐지 결과 파싱
            val detectionResult = parseYoloV8Output(output0, inputShape, originalWidth, originalHeight)

            // 최종 시각화 이미지 생성
            val visualizedBitmap = visualizeResults(
                bitmap,
                classMasks,
                detectionResult.detections
            )

            return YoloResult(
                detections = detectionResult.detections,
                bitmap = visualizedBitmap
            )

        } catch (e: Exception) {
            Log.e("✅YOLO", "❌ 추론 중 오류 발생: ${e.message}")
            e.printStackTrace()
            return YoloResult(detections = emptyList(), bitmap = null)
        } finally {
            isRunning = false
        }
    }

    // 새로운 시각화 메서드 - Android의 Canvas 사용
    private fun visualizeResults(
        originalBitmap: Bitmap,
        classMasks: Array<Array<FloatArray>>,
        detections: List<YoloBox>
    ): Bitmap {
        // 원본 Bitmap을 Mat으로 변환
        val srcMat = Mat()
        Utils.bitmapToMat(originalBitmap, srcMat)

        // 각 클래스의 마스크에 대해 opencv로 그리기 수행
        for (classId in classMasks.indices) {
            // 마스크 배열을 Mat으로 변환
            val maskMat = convertMaskToMat(classMasks[classId])
            // 원본 크기로 리사이즈
            val resizedMaskMat = Mat()
            Imgproc.resize(
                maskMat,
                resizedMaskMat,
                Size(originalBitmap.width.toDouble(), originalBitmap.height.toDouble()),
                0.0, 0.0, Imgproc.INTER_LINEAR
            )

            // GaussianBlur 적용 (커널 크기는 상황에 맞게 조정)
            Imgproc.GaussianBlur(resizedMaskMat, resizedMaskMat, Size(5.0, 5.0), 0.0)

            // 이진화: 마스크 값을 임계값(MASK_THRESHOLD) 기준으로 이진화
            val binaryMat = Mat()
            Imgproc.threshold(resizedMaskMat, binaryMat, MASK_THRESHOLD, 1.0, Imgproc.THRESH_BINARY)
            val mask8U = Mat()
            binaryMat.convertTo(mask8U, CvType.CV_8U, 255.0)

            // 윤곽선 추출
            val contours = mutableListOf<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(mask8U, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

            // 노이즈 제거를 위해 작은 영역은 무시하고 opencv로 윤곽선 그리기
            val significantContours = contours.filter { contour ->
                Imgproc.contourArea(contour) > 1000.0
            }
            if (significantContours.isNotEmpty()) {
                Imgproc.drawContours(srcMat, significantContours, -1, CLASS_COLORS[classId], 3)
            }

            // 사용한 Mat 자원 해제
            maskMat.release()
            resizedMaskMat.release()
            binaryMat.release()
            mask8U.release()
            hierarchy.release()
            for (contour in contours) {
                contour.release()
            }
        }

        // 탐지된 객체(바운딩 박스) 그리기
        for (box in detections) {
            val left = box.x - box.width / 2
            val top = box.y - box.height / 2
            val right = box.x + box.width / 2
            val bottom = box.y + box.height / 2

            // 바운딩 박스 그리기 (노란색)
            Imgproc.rectangle(
                srcMat,
                org.opencv.core.Point(left.toDouble(), top.toDouble()),
                org.opencv.core.Point(right.toDouble(), bottom.toDouble()),
                Scalar(0.0, 255.0, 255.0, 255.0),
                4
            )

            // 클래스 레이블 및 신뢰도 추가
            val className = if (box.classId == FRET_INDEX) "Fret" else "Nut"
            val label = "$className: ${String.format("%.1f%%", box.confidence * 100)}"
            Imgproc.putText(
                srcMat,
                label,
                org.opencv.core.Point(left.toDouble(), top.toDouble() - 10),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                1.0,
                Scalar(255.0, 255.0, 255.0),
                2
            )
        }

        // 최종 결과 Mat을 Bitmap으로 변환하여 반환
        val outputBitmap = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(srcMat, outputBitmap)
        srcMat.release()
        return outputBitmap
    }

    // 마스크를 OpenCV Mat으로 변환
    private fun convertMaskToMat(mask: Array<FloatArray>): Mat {
        val mat = Mat(mask.size, mask[0].size, CvType.CV_32F)
        for (y in mask.indices) {
            for (x in mask[0].indices) {
                mat.put(y, x, mask[y][x].toDouble())
            }
        }
        return mat
    }

    // 이미지를 모델 입력 형식으로 변환
    private fun prepareInputBuffer(bitmap: Bitmap, inputShape: IntArray): ByteBuffer {
        val height = inputShape[1]
        val width = inputShape[2]
        val channels = inputShape[3]

        Log.d("✅YOLO", "모델 입력 크기: 높이=$height, 너비=$width, 채널=$channels")

        // 이미지 크기 조정 (원본 비율 유지)
        val resizedBitmap = resizeBitmapWithAspectRatio(bitmap, width, height)

        // 바이트 버퍼 생성
        val byteBuffer = ByteBuffer.allocateDirect(1 * height * width * channels * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        // 비트맵 픽셀 배열 준비
        val intValues = IntArray(width * height)
        resizedBitmap.getPixels(intValues, 0, width, 0, 0, width, height)

        // RGB 정규화 값
        val mean = floatArrayOf(0f, 0f, 0f)
        val std = floatArrayOf(255f, 255f, 255f)

        // 픽셀 변환 (NHWC 형식)
        for (i in 0 until height) {
            for (j in 0 until width) {
                val pixelValue = intValues[i * width + j]

                val r = (pixelValue shr 16) and 0xFF
                val g = (pixelValue shr 8) and 0xFF
                val b = pixelValue and 0xFF

                byteBuffer.putFloat((r - mean[0]) / std[0])
                byteBuffer.putFloat((g - mean[1]) / std[1])
                byteBuffer.putFloat((b - mean[2]) / std[2])
            }
        }

        // 버퍼 위치 되돌리기
        byteBuffer.rewind()

        // 임시 비트맵 메모리 해제
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }

        return byteBuffer
    }

    // 원본 비율을 유지하면서 이미지 크기 조정 (검은색 패딩 추가)
    private fun resizeBitmapWithAspectRatio(original: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = original.width
        val originalHeight = original.height

        val ratio = min(
            targetWidth.toFloat() / originalWidth.toFloat(),
            targetHeight.toFloat() / originalHeight.toFloat()
        )

        val scaledWidth = (originalWidth * ratio).toInt()
        val scaledHeight = (originalHeight * ratio).toInt()

        // 중간 크기로 비트맵 리사이징
        val scaledBitmap = Bitmap.createScaledBitmap(
            original,
            scaledWidth,
            scaledHeight,
            true
        )

        // 타겟 크기의 빈 비트맵 (검은색) 생성
        val resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.BLACK)

        // 가운데 배치
        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f

        // 리사이즈된 이미지 그리기
        canvas.drawBitmap(scaledBitmap, left, top, null)

        // 중간 비트맵 메모리 해제
        if (scaledBitmap != original) {
            scaledBitmap.recycle()
        }

        return resultBitmap
    }

    // YOLOv8 출력 파싱
    private fun parseYoloV8Output(
        output: Array<Array<FloatArray>>,
        inputShape: IntArray,
        originalWidth: Int,
        originalHeight: Int
    ): YoloResult {
        try {
            // 바운딩 박스 관련 인덱스
            val boxIndices = 0..3  // x, y, width, height
            val confidenceIndex = 4  // objectness score
            val classStartIndex = 5  // 클래스 시작 인덱스
            val numClasses = 2  // 클래스 수

            // 결과 저장 변수
            val detections = mutableListOf<YoloBox>()

            // 모든 탐지에 대해 순회
            for (i in 0 until output[0][confidenceIndex].size) {
                val confidence = output[0][confidenceIndex][i]

                // 임계값 이상인 경우에만 처리
                if (confidence > CONFIDENCE_THRESHOLD) {
                    // 가장 높은 클래스 확률 찾기
                    var bestClassId = -1
                    var bestClassScore = 0f

                    for (c in 0 until numClasses) {
                        val classIndex = classStartIndex + c
                        if (classIndex < output[0].size) {
                            val score = output[0][classIndex][i] * confidence
                            if (score > bestClassScore) {
                                bestClassScore = score
                                bestClassId = c
                            }
                        }
                    }

                    // 클래스가 탐지되었으면 박스 정보 추출
                    if (bestClassId >= 0 && bestClassScore > CONFIDENCE_THRESHOLD) {
                        // 정규화된 좌표 가져오기
                        val centerX = output[0][0][i]
                        val centerY = output[0][1][i]
                        val width = output[0][2][i]
                        val height = output[0][3][i]

                        // 좌표 변환 - 비율 정보 반영
                        // 모델 입력 이미지에 패딩이 포함되어 있으므로 조정 필요
                        val inputWidth = inputShape[2].toFloat()
                        val inputHeight = inputShape[1].toFloat()

                        // 패딩을 고려한 스케일 계산
                        val scaleX = originalWidth.toFloat() / inputWidth
                        val scaleY = originalHeight.toFloat() / inputHeight

                        // 실제 이미지 크기에 맞게 좌표 변환
                        val realX = centerX * originalWidth
                        val realY = centerY * originalHeight
                        val realW = width * originalWidth
                        val realH = height * originalHeight

                        // 박스 정보 저장
                        detections.add(
                            YoloBox(
                                classId = bestClassId,
                                confidence = bestClassScore,
                                x = realX,
                                y = realY,
                                width = realW,
                                height = realH
                            )
                        )
                    }
                }
            }

            Log.d("✅YOLO", "탐지된 객체 수: ${detections.size}")
            return YoloResult(detections = detections, bitmap = null)

        } catch (e: Exception) {
            Log.e("✅YOLO", "결과 파싱 오류: ${e.message}")
            e.printStackTrace()
            return YoloResult(detections = emptyList(), bitmap = null)
        }
    }

    // 실행 관리 함수들
    fun stop() {
        shouldRun = false
    }

    fun isRunningAllowed(): Boolean = shouldRun

    // 리소스 해제
    override fun close() {
        synchronized(this) {
            while (isRunning) {
                Log.w("✅YOLO", "❗추론 중이라 인터프리터 닫기 대기")
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    break
                }
            }

            try {
                interpreter?.close()
                interpreter = null
                Log.d("✅YOLO", "✅ 인터프리터 정상 해제 완료")
            } catch (e: Exception) {
                Log.e("✅YOLO", "리소스 해제 중 오류: ${e.message}")
            }
        }
    }
}