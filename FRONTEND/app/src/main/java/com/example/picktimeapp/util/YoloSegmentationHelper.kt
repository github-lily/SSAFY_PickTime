package com.example.picktimeapp.util

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.picktimeapp.data.model.YoloResult
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.Closeable

class YoloSegmentationHelper(private val context: Context) : Closeable {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private val modelName = "ai/yolov8n-seg.tflite"
    private val inputSize = 640

    // 이미지 전처리를 위한 프로세서
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    init {
        try {
            loadModel()
        } catch (e: Exception) {
            Log.e("✅YOLO", "모델 로딩 실패: ${e.message}")
        }
    }

    private fun loadModel() {
        try {
            // GPU 지원 확인 및 설정
            val compatList = CompatibilityList()
            // 기본 옵션으로 인터프리터 생성 (CPU 사용)
            val options = Interpreter.Options()
            // 필요하다면 스레드 수를 늘려 CPU 성능을 향상
            options.setNumThreads(4)

            // 모델 파일 로드 (TFLite Support 라이브러리 활용)
            val modelFile = FileUtil.loadMappedFile(context, modelName)
            interpreter = Interpreter(modelFile, options)

            Log.d("✅YOLO", "모델 로딩 성공: $modelName")
        } catch (e: Exception) {
            Log.e(TAG, "모델 로딩 실패", e)
            throw e
    }}
//        val options = Interpreter.Options().apply {
//            if (compatList.isDelegateSupportedOnThisDevice) {
//                gpuDelegate = GpuDelegate()
//                addDelegate(gpuDelegate)
//                Log.d("✅YOLO", "GPU 가속 활성화")
//            } else {
//                // CPU 최적화 설정
//                setNumThreads(4)
//                Log.d("✅YOLO", "CPU 최적화 (4 스레드)")
//            }
//        }

//        // 모델 파일 로드 (TFLite Support 라이브러리 활용)
//    fun loadModelFile() {
//        try {
//            val modelFile = FileUtil.loadMappedFile(context, modelName)
//            interpreter = Interpreter(modelFile, options)
//
//            Log.d("✅YOLO", "모델 로딩 성공: $modelName")
//        } catch (e: Exception) {
//            Log.e("✅YOLO", "모델 파일 로딩 실패: ${e.message}")
//            throw e
//        }
//    }

    fun runInference(bitmap: Bitmap): YoloResult {
        val interpreter = this.interpreter ?: run {
            Log.e("YOLO", "인터프리터가 초기화되지 않았습니다")
            return YoloResult.None
        }

        try {
            // TFLite Support 라이브러리를 사용한 이미지 변환
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val processedImage = imageProcessor.process(tensorImage)

            // 입력값 테스트용 로그
            Log.d("✅YOLO", "입력 이미지 사이즈: ${bitmap.width}x${bitmap.height}")
            Log.d("✅YOLO", "TensorImage dtype: ${processedImage.dataType}")
            Log.d("✅YOLO", "Tensor buffer size: ${processedImage.buffer.remaining()}")

            // 출력 버퍼 설정 (모델에 맞게 조정 필요)
            val outputBuffer = Array(1) { Array(100) { FloatArray(6) } }

            // 입력값 테스트용 로그
            val inputShape = interpreter.getInputTensor(0).shape()
            val outputShape = interpreter.getOutputTensor(0).shape()
            Log.d("✅YOLO", "입력 텐서 shape: ${inputShape.contentToString()}")
            Log.d("✅YOLO", "출력 텐서 shape: ${outputShape.contentToString()}")

            // 추론 실행
            Log.d("✅YOLO", "모델 추론 시작")
            interpreter.run(processedImage.buffer, outputBuffer)
            Log.d("✅YOLO", "모델 추론 완료")

            return parseYoloOutput(outputBuffer[0])
        } catch (e: Exception) {
            Log.e("✅YOLO", "추론 중 오류 발생: ${e.message}")
            e.printStackTrace()
            return YoloResult.None
        }
    }

    private fun parseYoloOutput(output: Array<FloatArray>): YoloResult {
        try {
            // 임시 예시: 가장 confidence 높은 결과 하나만 추출
            val best = output.maxByOrNull { it[4] } ?: return YoloResult.None

            // 신뢰도가 너무 낮으면 무시
            if (best[4] < 0.5f) return YoloResult.None

            val classId = best[5].toInt()
            val conf = best[4]

            Log.d("✅YOLO", "분류 결과: 클래스=$classId, 신뢰도=$conf")
            return YoloResult.Class(classId, conf)
        } catch (e: Exception) {
            Log.e("✅YOLO", "결과 파싱 오류: ${e.message}")
            return YoloResult.None
        }
    }

    override fun close() {
        try {
            interpreter?.close()
            gpuDelegate?.close()
        } catch (e: Exception) {
            Log.e("✅YOLO", "리소스 해제 중 오류: ${e.message}")
        }
    }
}