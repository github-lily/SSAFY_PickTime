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

    // 추론을 실행할 때 사용할 모델 인터프리터를 담을 변수
    private var interpreter: Interpreter? = null
    private val modelName = "ai/best_v3_1920_720.tflite"


    init {
        try {
            loadModel()
        } catch (e: Exception) {
            Log.e("✅YOLO", "모델 로딩 실패: ${e.message}")
        }
    }

    // 🔥 모델 불러오기 🔥
    private fun loadModel() {
        try {
            // 모델을 실행할 때 사용할 옵션 설정 객체
            val options = Interpreter.Options()
            // CPU 성능 최적화를 위한 스레드 설정
            options.setNumThreads(4)
            Log.d("✅YOLO", "CPU 최적화 (4 스레드)")

            // 모델 파일을 메모리로 로드
            val modelFile = FileUtil.loadMappedFile(context, modelName)
            // 모델을 인터프리터에 연결
            interpreter = Interpreter(modelFile, options)

            // 입력과 출력의 데이터 모양(shape) 확인
            val inputTensor = interpreter?.getInputTensor(0)
            val outputTensor = interpreter?.getOutputTensor(0)
            Log.d("✅YOLO", "모델 로드 성공: 입력 형태=${inputTensor?.shape()?.contentToString()}, 출력 형태=${outputTensor?.shape()?.contentToString()}")

        } catch (e: Exception) {
            Log.e(TAG, "모델 로딩 실패", e)
            throw e
        }
    }

    // 🔥 모델로 실제 추론 실행(YOLO 모델이 이미지 분석해서 결과를 주는 부분) 🔥
    fun runInference(bitmap: Bitmap): YoloResult {
        val interpreter = this.interpreter ?: run {
            // 모델을 아직 못불러왔을 때 에러 반환
            Log.e("✅YOLO", "인터프리터가 초기화되지 않았습니다")
            return YoloResult.None
        }

        try {
            // 입력 텐서 정보 가져오기(모델이 원하는 입력 형태(shape) 확인)
            val inputShape = interpreter.getInputTensor(0).shape()
            Log.d("✅YOLO", "실제 입력 텐서 모양: ${inputShape.contentToString()}")

            // 이미지(bitmap) 을 모델이 이해할 수 있는 숫자 버퍼(ByteBuffer)로 변환
            val inputBuffer = prepareInputBuffer(bitmap, inputShape)

            // 모델이 결과를 어떤 형태로 알려줄지 확인
            val outputShape = interpreter.getOutputTensor(0).shape()
            Log.d("✅YOLO", "출력 텐서 모양: ${outputShape.contentToString()}")

            // 출력 버퍼 만들기
            // 모델 출력 모양(shape)에 따라 결과를 담을 outputBuffer 생성
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

            // 추론 실행(모델에 이미지를 입력하고 결과를 출력 버퍼에 저장)
            Log.d("✅YOLO", "모델 추론 시작")
            interpreter.run(inputBuffer, outputBuffer)
            Log.d("✅YOLO", "모델 추론 완료")

            // 결과 파싱(결과 해석)
            return parseYoloOutput(outputBuffer, outputShape)

        } catch (e: Exception) {
            Log.e("✅YOLO", "추론 중 오류 발생: ${e.message}")
            e.printStackTrace()
            return YoloResult.None
        }
    }

    // 🔥 이미지를 모델이 이해할 수 있는 숫자 형태로 변환 🔥
    private fun prepareInputBuffer(bitmap: Bitmap, inputShape: IntArray): ByteBuffer {
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
                height = 360
                width = 640
                channels = 3
            }
        } else {
            // 기본값
            height = 360
            width = 640
            channels = 3
        }

        Log.d("✅YOLO", "준비 중인 입력 버퍼 크기: 높이=$height, 너비=$width, 채널=$channels")

        // 입력 이미지를 모델 입력 크기로 조정
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        // 바이트 버퍼 생성 (float32 형식)
        // 크기에 맞는 숫자 저장 공간(ByteBuffer) 생성
        val byteBuffer = ByteBuffer.allocateDirect(1 * height * width * channels * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        // 비트맵을 바이트 버퍼로 변환 (정규화 포함)
        // 이미지를 픽셀 단위로 숫자 배열로 바꿔줌(0xAARRGGBB)
        val intValues = IntArray(width * height)
        resizedBitmap.getPixels(intValues, 0, width, 0, 0, width, height)

        // NHWC 형식 (일반적인 TFLite 모델)
        if (inputShape.size < 4 || inputShape[1] > 3) {
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixelValue = intValues[i * width + j]
                    // 픽셀의 RGB 정규화 (0-255 -> 0-1)
                    byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f) // R
                    byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)  // G
                    byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)          // B
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

        // 버퍼의 위치를 처음으로 되돌리고 반환
        byteBuffer.rewind()
        return byteBuffer
    }

    // 🔥 모델이 준 결과를 해석해서 우리가 쓸 수 있도록 정리하는 부분 🔥
    private fun parseYoloOutput(output: Any, outputShape: IntArray): YoloResult {
        try {
            // 출력 형식에 따라 결과를 다르게 처리
            when {
                // 배열인지, 빈배열은 아닌지, 배열 안에 배열이 있는지 확인. 즉 Array<Array<FloatArray>> 형태인지 확인
                output is Array<*> && output.isNotEmpty() && output[0] is Array<*> -> {
                    // 형 변환
                    @Suppress("UNCHECKED_CAST")
                    val detections = output as Array<Array<FloatArray>> // 3차원 배열 ex) [1][8400][84] -> 8400개 박스, 각 박스에 84개 숫자

                    // 감지 결과 배열이 몇 차원인지 확인
                    Log.d("✅YOLO", "출력 파싱 중: ${detections.size} x ${detections[0].size} x ${detections[0][0].size}")

                    // 클래수 개수 계산
                    val numClasses = detections[0][0].size - 5
                    // 앞의 5개는 박스 정보[x, y, w, h, conf,class1,...]이기 때문에 `-5`

                    // 초기값 설정
                    // 가장 높은 확률의 클래스 ID와 신뢰도를 저장할 변수
                    var bestClassId = -1
                    var bestConfidence = 0f

                    // 출력 구조에 따라 다르게 처리
                    if (detections[0].size > 84 && detections[0][0].size <= 84) {
                        val confidenceIndex = 4 // 일반적으로 신뢰도는 5번째 값

                        // 순회하며 신뢰도가 가장 높은 것 찾기
                        for (i in detections[0].indices) {
                            val confidence = detections[0][i][confidenceIndex]
                            if (confidence > bestConfidence) {
                                bestConfidence = confidence

                                // 클래스 중 가장 점수 높은 것 뽑기
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
                        val confidenceRow = 4 // 5번째 행에 신뢰도 위치

                        // 열을 순회하며 제일 확률 높은 박스 찾기
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

                    // 결과 반환(신뢰도가 50% 이상일 때 결과 반환)
                    if (bestConfidence > 0.5f) {
                        Log.d("✅YOLO", "탐지 결과: 클래스=$bestClassId, 신뢰도=$bestConfidence")
                        return YoloResult.Class(bestClassId, bestConfidence)
                    }
                }
            }
            // 아무것도 못찾았을 때
            Log.d("✅YOLO", "아무것도 못찾음")
            return YoloResult.None
        } catch (e: Exception) {
            Log.e("✅YOLO", "결과 파싱 오류: ${e.message}")
            e.printStackTrace()
            return YoloResult.None
        }
    }

    // 앱이 종료되거나 클래스 사용이 끝나면 모델 리소스를 정리하여 메모리 누수 방지
    override fun close() {
        try {
            interpreter?.close()
        } catch (e: Exception) {
            Log.e("✅YOLO", "리소스 해제 중 오류: ${e.message}")
        }
    }
}