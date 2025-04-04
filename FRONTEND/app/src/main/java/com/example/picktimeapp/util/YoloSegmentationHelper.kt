package com.example.picktimeapp.util

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.picktimeapp.data.model.YoloResult
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

class YoloSegmentationHelper(private val context: Context) : Closeable {

    private var interpreter: Interpreter? = null
    private val modelName = "ai/yolov8n-seg_v3_16_9.tflite"
    private val inputSize = 640

    init {
        try {
            loadModel()
        } catch (e: Exception) {
            Log.e("✅YOLO", "모델 로딩 실패: ${e.message}")
        }
    }

    private fun loadModel() {
        try {
            // CPU 기반 인터프리터 생성
            val options = Interpreter.Options()
            // CPU 성능 최적화를 위한 스레드 설정
            options.setNumThreads(4)
            Log.d("✅YOLO", "CPU 최적화 (4 스레드)")

            // 모델 파일 로드
            val modelFile = FileUtil.loadMappedFile(context, modelName)
            interpreter = Interpreter(modelFile, options)

            // 모델 정보 로깅
            val inputTensor = interpreter?.getInputTensor(0)
            val outputTensor = interpreter?.getOutputTensor(0)
            Log.d("✅YOLO", "모델 로드 성공: 입력 형태=${inputTensor?.shape()?.contentToString()}, 출력 형태=${outputTensor?.shape()?.contentToString()}")

        } catch (e: Exception) {
            Log.e(TAG, "모델 로딩 실패", e)
            throw e
        }
    }

    fun runInference(bitmap: Bitmap): YoloResult {
        val interpreter = this.interpreter ?: run {
            Log.e("YOLO", "인터프리터가 초기화되지 않았습니다")
            return YoloResult.None
        }

        try {
            // 입력 텐서 정보 가져오기
            val inputShape = interpreter.getInputTensor(0).shape()
            Log.d("✅YOLO", "실제 입력 텐서 모양: ${inputShape.contentToString()}")

            // 입력 이미지 준비 (inputShape에 맞게 조정)
            val inputBuffer = prepareInputBuffer(bitmap, inputShape)

            // 출력 버퍼 준비 (실제 모델 출력에 맞게 조정 필요)
            val outputShape = interpreter.getOutputTensor(0).shape()
            Log.d("✅YOLO", "출력 텐서 모양: ${outputShape.contentToString()}")

            // 출력 유형에 따라 적절한 버퍼 생성
            // 일반 YOLO 출력 형식: [batch, 84, 8400] for YOLO v8
            val outputBuffer: Any = when {
                outputShape.size == 3 -> {
                    if (outputShape[1] <= 100) {
                        // 작은 출력 크기 - 일반적인 객체 감지 결과
                        Array(outputShape[0]) { Array(outputShape[1]) { FloatArray(outputShape[2]) } }
                    } else {
                        // 큰 출력 크기 - 예: [1, 84, 8400]
                        Array(outputShape[0]) { Array(outputShape[1]) { FloatArray(outputShape[2]) } }
                    }
                }
                outputShape.size == 2 -> {
                    Array(outputShape[0]) { FloatArray(outputShape[1]) }
                }
                else -> {
                    // 다른 출력 형식 (fallback)
                    Array(1) { Array(100) { FloatArray(6) } }
                }
            }

            // 추론 실행
            Log.d("✅YOLO", "모델 추론 시작")
            interpreter.run(inputBuffer, outputBuffer)
            Log.d("✅YOLO", "모델 추론 완료")

            // 결과 파싱
            return parseYoloOutput(outputBuffer, outputShape)

        } catch (e: Exception) {
            Log.e("✅YOLO", "추론 중 오류 발생: ${e.message}")
            e.printStackTrace()
            return YoloResult.None
        }
    }

    private fun prepareInputBuffer(bitmap: Bitmap, inputShape: IntArray): ByteBuffer {
        // YOLOv8 모델은 일반적으로 [1, 3, 640, 640] (NCHW) 또는 [1, 640, 640, 3] (NHWC) 형식
        val height: Int
        val width: Int
        val channels: Int

        if (inputShape.size == 4) {
            // 입력 형태가 [batch, height, width, channels] (NHWC)인 경우
            if (inputShape[1] > 3 && inputShape[3] <= 3) {
                height = inputShape[1]
                width = inputShape[2]
                channels = inputShape[3]
            }
            // 입력 형태가 [batch, channels, height, width] (NCHW)인 경우
            else if (inputShape[1] <= 3) {
                height = inputShape[2]
                width = inputShape[3]
                channels = inputShape[1]
            } else {
                // 기본값
                height = inputSize
                width = inputSize
                channels = 3
            }
        } else {
            // 기본값
            height = inputSize
            width = inputSize
            channels = 3
        }

        Log.d("✅YOLO", "준비 중인 입력 버퍼 크기: 높이=$height, 너비=$width, 채널=$channels")

        // 입력 이미지를 모델 입력 크기로 조정
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        // 바이트 버퍼 생성 (float32 형식)
        val byteBuffer = ByteBuffer.allocateDirect(1 * height * width * channels * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        // 비트맵을 바이트 버퍼로 변환 (정규화 포함)
        val intValues = IntArray(width * height)
        resizedBitmap.getPixels(intValues, 0, width, 0, 0, width, height)

        // NHWC 형식 (일반적인 TFLite 모델)
        if (inputShape.size < 4 || inputShape[1] > 3) {
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixelValue = intValues[i * width + j]
                    // RGB 정규화 (0-255 -> 0-1)
                    byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
                    byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
                    byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
                }
            }
        }
        // NCHW 형식 (PyTorch에서 변환된 모델)
        else {
            // 모든 R 채널
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixelValue = intValues[i * width + j]
                    byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
                }
            }
            // 모든 G 채널
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixelValue = intValues[i * width + j]
                    byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
                }
            }
            // 모든 B 채널
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixelValue = intValues[i * width + j]
                    byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
                }
            }
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    private fun parseYoloOutput(output: Any, outputShape: IntArray): YoloResult {
        try {
            // 출력 형식에 따라 결과를 다르게 처리

            // YOLO v8 일반적인 출력 형식: [1, 84, 8400] 또는 [1, 8400, 84]
            when {
                output is Array<*> && output.isNotEmpty() && output[0] is Array<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val detections = output as Array<Array<FloatArray>>

                    Log.d("✅YOLO", "출력 파싱 중: ${detections.size} x ${detections[0].size} x ${detections[0][0].size}")

                    // YOLO 출력에서 클래스와 신뢰도 추출
                    val numClasses = detections[0][0].size - 5 // 일반적으로 마지막 5개를 제외한 나머지가 클래스 수

                    var bestClassId = -1
                    var bestConfidence = 0f

                    // 출력 구조에 따라 다르게 처리
                    if (detections[0].size > 84 && detections[0][0].size <= 84) {
                        // [1, 8400, 84] 형식 (각 감지마다 84개 값)
                        val confidenceIndex = 4 // 일반적으로 신뢰도는 5번째 값

                        for (i in detections[0].indices) {
                            val confidence = detections[0][i][confidenceIndex]
                            if (confidence > bestConfidence) {
                                bestConfidence = confidence

                                // 클래스 ID 찾기 (가장 높은 클래스 점수)
                                var maxClassScore = 0f
                                var maxClassIndex = 0
                                for (c in 0 until numClasses) {
                                    val classScore = detections[0][i][confidenceIndex + 1 + c]
                                    if (classScore > maxClassScore) {
                                        maxClassScore = classScore
                                        maxClassIndex = c
                                    }
                                }
                                bestClassId = maxClassIndex
                            }
                        }
                    } else if (detections[0].size <= 84 && detections[0][0].size > 84) {
                        // [1, 84, 8400] 형식 (각 값에 대해 8400개 감지)
                        val confidenceRow = 4 // 신뢰도 행

                        for (i in detections[0][confidenceRow].indices) {
                            val confidence = detections[0][confidenceRow][i]
                            if (confidence > bestConfidence) {
                                bestConfidence = confidence

                                // 클래스 ID 찾기
                                var maxClassScore = 0f
                                var maxClassIndex = 0
                                for (c in 0 until numClasses) {
                                    val classScore = detections[0][confidenceRow + 1 + c][i]
                                    if (classScore > maxClassScore) {
                                        maxClassScore = classScore
                                        maxClassIndex = c
                                    }
                                }
                                bestClassId = maxClassIndex
                            }
                        }
                    }

                    // 결과 반환
                    if (bestConfidence > 0.5f) {
                        Log.d("✅YOLO", "탐지 결과: 클래스=$bestClassId, 신뢰도=$bestConfidence")
                        return YoloResult.Class(bestClassId, bestConfidence)
                    }
                }
            }

            return YoloResult.None
        } catch (e: Exception) {
            Log.e("✅YOLO", "결과 파싱 오류: ${e.message}")
            e.printStackTrace()
            return YoloResult.None
        }
    }

    override fun close() {
        try {
            interpreter?.close()
        } catch (e: Exception) {
            Log.e("✅YOLO", "리소스 해제 중 오류: ${e.message}")
        }
    }
}