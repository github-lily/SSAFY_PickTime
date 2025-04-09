//package com.example.picktimeapp.util
//
//import android.content.ContentValues.TAG
//import android.content.Context
//import android.graphics.Bitmap
//import android.util.Log
//import com.example.picktimeapp.data.model.YoloResult
//import org.tensorflow.lite.Interpreter
//import org.tensorflow.lite.support.common.FileUtil
//import java.io.Closeable
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import kotlin.math.exp
//import kotlin.math.max
//import kotlin.math.min
//
//class YoloSegmentationHelper(private val context: Context) : Closeable {
//
//    @Volatile
//    private var isRunning = false
//
//    @Volatile
//    private var shouldRun = true
//    // 추론을 실행할 때 사용할 모델 인터프리터를 담을 변수
//    private var interpreter: Interpreter? = null
//    private val modelName = "ai/best_1280_736_float32.tflite"
//    init {
//        try {
//            loadModel()
//        } catch (e: Exception) {
//            Log.e("✅YOLO", "모델 로딩 실패: ${e.message}")
//        }
//    }
//
//    // 🔥 모델 불러오기 🔥
//    private fun loadModel() {
//        try {
//            // 모델을 실행할 때 사용할 옵션 설정 객체
//            val options = Interpreter.Options()
//            // CPU 성능 최적화를 위한 스레드 설정
//            options.setNumThreads(4)
//            Log.d("✅YOLO", "CPU 최적화 (4 스레드)")
//
//            // 모델 파일을 메모리로 로드
//            val modelFile = FileUtil.loadMappedFile(context, modelName)
//            // 모델을 인터프리터에 연결
//            interpreter = Interpreter(modelFile, options)
//
//            // 입력과 출력의 데이터 모양(shape) 확인
//            val inputTensor = interpreter?.getInputTensor(0)
//            val outputTensor = interpreter?.getOutputTensor(0)
//            Log.d("✅YOLO", "모델 로드 성공: 입력 형태=${inputTensor?.shape()?.contentToString()}, 출력 형태=${outputTensor?.shape()?.contentToString()}")
//            Log.d("✅YOLO", "입력 데이터 타입: ${inputTensor?.dataType()}, 출력 데이터 타입: ${outputTensor?.dataType()}")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "모델 로딩 실패", e)
//            throw e
//        }
//    }
//
//    // 🔥 모델로 실제 추론 실행(YOLO 모델이 이미지 분석해서 결과를 주는 부분) 🔥
//    fun runInference(bitmap: Bitmap): YoloResult {
//        val interpreter = this.interpreter ?: run {
//            Log.e("✅YOLO", "❌ 인터프리터가 초기화되지 않음")
//            return YoloResult.None
//        }
//
//        // 이미 추론 중이면 중복 실행 방지
//        synchronized(this) {
//            if (isRunning) {
//                Log.w("✅YOLO", "❗이미 추론 중이라 스킵")
//                return YoloResult.None
//            }
//            isRunning = true
//        }
//
//        try {
//            // 1. 입력 텐서 가져오기
//            val inputTensor = interpreter.getInputTensor(0)
//            val inputShape = inputTensor.shape()
//            Log.d("✅YOLO", "실제 입력 텐서 모양: ${inputShape.contentToString()}")
//
//            // 2. 원본 이미지 크기 기록
//            Log.d("✅YOLO", "원본 이미지 크기: ${bitmap.width} x ${bitmap.height}")
//
//            // 3. 이미지를 모델이 이해할 수 있는 숫자 버퍼로 변환
//            val inputBuffer = prepareInputBuffer(bitmap, inputShape)
//            Log.d("✅YOLO", "Input buffer capacity: ${inputBuffer.capacity()}")
//
//            // 4. 출력 텐서 정보 가져오기
//            val outputTensor = interpreter.getOutputTensor(0)
//            val outputShape = outputTensor.shape()
//            Log.d("✅YOLO", "출력 텐서 모양: ${outputShape.contentToString()}")
//            Log.d("✅YOLO", "출력 데이터 타입: ${outputTensor.dataType()}")
//
//            // 5. YOLOv8의 출력에 맞춘 버퍼 생성
//            val outputBuffer = Array(outputShape[0]) {
//                Array(outputShape[1]) {
//                    FloatArray(outputShape[2])
//                }
//            }
//
//            // 6. 모델 추론 실행
//            Log.d("✅YOLO", "모델 추론 시작")
//            try {
//                interpreter.run(inputBuffer, outputBuffer)  // 모델 추론 실행
//                Log.d("✅YOLO", "모델 추론 완료")
//
//                // 출력 값 샘플 로깅 (최대값, 평균값 등)
//                var maxVal = Float.MIN_VALUE
//                var minVal = Float.MAX_VALUE
//                var sum = 0.0
//                var count = 0
//
//                // 일부 샘플만 확인 (디버깅용)
//                for (i in 0 until min(5, outputShape[1])) {
//                    for (j in 0 until min(100, outputShape[2])) {
//                        val value = outputBuffer[0][i][j]
//                        maxVal = max(maxVal, value)
//                        minVal = min(minVal, value)
//                        sum += value
//                        count++
//                    }
//                }
//
//                Log.d("✅YOLO", "출력 샘플 통계: 최소=$minVal, 최대=$maxVal, 평균=${sum/count}")
//
//            } catch (e: IllegalStateException) {
//                Log.e("✅YOLO", "인터프리터가 닫힌 상태에서 추론 시도됨: ${e.message}")
//                return YoloResult.None
//            }
//
//            // 7. 출력 결과 파싱
//            return parseYoloV8Output(outputBuffer, outputShape, bitmap.width, bitmap.height)
//
//        } catch (e: Exception) {
//            Log.e("✅YOLO", "❌ 추론 중 오류 발생: ${e.message}")
//            return YoloResult.None
//        } finally {
//            // ✅ 반드시 false로 해제해줘야 다음 추론/종료 가능
//            isRunning = false
//        }
//    }
//
//
//    // 🔥 이미지를 모델이 이해할 수 있는 숫자 형태로 변환 🔥
//    private fun prepareInputBuffer(bitmap: Bitmap, inputShape: IntArray): ByteBuffer {
//        // YOLOv8의 표준 입력 크기는 [batch, height, width, channels] 형식
//        val height = inputShape[1]
//        val width = inputShape[2]
//        val channels = inputShape[3]
//
//        Log.d("✅YOLO", "준비 중인 입력 버퍼 크기: 높이=$height, 너비=$width, 채널=$channels")
//
//        // 입력 이미지를 모델 입력 크기로 조정
//        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
//
//        // 바이트 버퍼 생성 (float32 형식)
//        val byteBuffer = ByteBuffer.allocateDirect(1 * height * width * channels * 4)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        // 비트맵을 바이트 버퍼로 변환
//        val intValues = IntArray(width * height)
//        resizedBitmap.getPixels(intValues, 0, width, 0, 0, width, height)
//
//        // YOLOv8 정규화를 위한 값 (경우에 따라 다를 수 있음)
//        val mean = floatArrayOf(0f, 0f, 0f)  // 많은 모델에서 0이지만, 모델에 따라 다를 수 있음
//        val std = floatArrayOf(255f, 255f, 255f)  // 기본 정규화는 0-1 범위
//
//        // NHWC 형식으로 변환 (YOLOv8 표준)
//        for (i in 0 until height) {
//            for (j in 0 until width) {
//                val pixelValue = intValues[i * width + j]
//
//                // RGB 채널 추출
//                val r = (pixelValue shr 16) and 0xFF
//                val g = (pixelValue shr 8) and 0xFF
//                val b = pixelValue and 0xFF
//
//                // 정규화된 값 추가
//                byteBuffer.putFloat((r - mean[0]) / std[0])
//                byteBuffer.putFloat((g - mean[1]) / std[1])
//                byteBuffer.putFloat((b - mean[2]) / std[2])
//            }
//        }
//
//        // 로그에 입력 버퍼의 처음 몇 가지 값을 출력해서 디버깅
//        byteBuffer.rewind()
//        val debugValues = StringBuilder("입력 버퍼 샘플값: ")
//        for (i in 0 until min(10, height * width * channels)) {
//            if (i > 0) debugValues.append(", ")
//            debugValues.append(byteBuffer.getFloat(i * 4))
//        }
//        Log.d("✅YOLO", debugValues.toString())
//
//        // 버퍼의 위치를 처음으로 되돌리고 반환
//        byteBuffer.rewind()
//        return byteBuffer
//    }
//
//    // 🔥 YOLOv8 세그멘테이션 모델 결과 파싱 🔥
//    private fun parseYoloV8Output(
//        output: Array<Array<FloatArray>>,
//        outputShape: IntArray,
//        originalWidth: Int,
//        originalHeight: Int
//    ): YoloResult {
//        try {
//            Log.d("✅YOLO", "출력 파싱 중: ${output.size} x ${output[0].size} x ${output[0][0].size}")
//
//            // YOLOv8 세그멘테이션 출력 구조 이해하기
//            // 각 그리드 셀마다 detection 결과가 있음
//            // 첫 번째 차원은 배치 크기 (보통 1)
//            // 두 번째 차원 (38)은 feature vectors의 차원 수
//            // 세 번째 차원 (19320)은 그리드 셀의 수 * 앵커 수
//
//            // 바운딩 박스 관련 인덱스
//            val boxIndices = 0..3  // x, y, width, height
//            val confidenceIndex = 4  // objectness score
//            val classStartIndex = 5  // 클래스 시작 인덱스
//            val numClasses = 2  // 실제 클래스 수에 맞게 조정 (필요에 따라 변경)
//
//            // 최고 탐지 결과를 저장할 변수들
//            var maxConfidence = 0f
//            var maxConfidenceIndex = -1
//            var bestClassId = -1
//
//            // 모든 detection 검사
//            val confidenceThreshold = 0.25f
//
//            // 로그 출력을 위한 상위 5개 값 추적
//            val topConfidences = mutableListOf<Pair<Float, Int>>()
//            for (i in 0 until 5) {
//                topConfidences.add(Pair(0f, 0))
//            }
//
//            // 모든 그리드 셀을 검사
//            for (i in 0 until output[0][confidenceIndex].size) {
//                val confidence = output[0][confidenceIndex][i]
//
//                // 상위 5개 값 업데이트
//                if (confidence > topConfidences[0].first) {
//                    topConfidences[0] = Pair(confidence, i)
//                    topConfidences.sortBy { it.first }
//                }
//
//                // 최대 신뢰도 업데이트
//                if (confidence > maxConfidence) {
//                    maxConfidence = confidence
//                    maxConfidenceIndex = i
//                }
//            }
//
//            // 최고 신뢰도 값 5개 로깅
//            Log.d("✅YOLO", "상위 5개 신뢰도 값:")
//            topConfidences.forEachIndexed { index, (conf, idx) ->
//                if (idx < output[0][0].size) {  // 안전 체크
//                    Log.d("✅YOLO", "$index. 신뢰도: $conf, 인덱스: $idx, 좌표: (${output[0][0][idx]}, ${output[0][1][idx]})")
//                }
//            }
//
//            // 충분한 신뢰도를 가진 탐지가 있다면
//            if (maxConfidence > confidenceThreshold) {
//                // 최고 클래스 찾기
//                for (c in 0 until numClasses) {
//                    val classIndex = classStartIndex + c
//                    if (classIndex < output[0].size) {  // 배열 범위 체크
//                        val classConfidence = output[0][classIndex][maxConfidenceIndex] * maxConfidence
//                        if (classConfidence > confidenceThreshold) {
//                            bestClassId = c
//                            break
//                        }
//                    }
//                }
//
//                // 바운딩 박스 좌표가 유효한지 확인
//                if (maxConfidenceIndex >= 0 && maxConfidenceIndex < output[0][0].size) {
//                    // 바운딩 박스 좌표 (중심점 x,y와 너비,높이)
//                    val centerX = output[0][0][maxConfidenceIndex]
//                    val centerY = output[0][1][maxConfidenceIndex]
//                    val width = output[0][2][maxConfidenceIndex]
//                    val height = output[0][3][maxConfidenceIndex]
//
//                    Log.d("✅YOLO", "탐지 결과: 클래스=$bestClassId, 신뢰도=$maxConfidence")
//                    Log.d("✅YOLO", "박스 좌표 (정규화): centerX=$centerX, centerY=$centerY, width=$width, height=$height")
//
//                    // 실제 이미지 크기에 맞게 좌표 변환
//                    val realX = centerX * originalWidth
//                    val realY = centerY * originalHeight
//                    val realW = width * originalWidth
//                    val realH = height * originalHeight
//
//                    Log.d("✅YOLO", "박스 실제 좌표: centerX=$realX, centerY=$realY, width=$realW, height=$realH")
//
//                    return YoloResult.Class(bestClassId, maxConfidence)
//                }
//            }
//
//            // 낮은 신뢰도지만 디버깅을 위해 최고 값 정보 출력
//            Log.d("✅YOLO", "탐지 결과 없음: 최대 신뢰도=$maxConfidence, 인덱스=$maxConfidenceIndex")
//            if (maxConfidenceIndex >= 0 && maxConfidenceIndex < output[0][0].size) {
//                val centerX = output[0][0][maxConfidenceIndex]
//                val centerY = output[0][1][maxConfidenceIndex]
//                Log.d("✅YOLO", "최대 신뢰도 위치 (정규화): x=$centerX, y=$centerY")
//
//                // 첫 몇 개 클래스에 대한 값 출력
//                for (c in 0 until min(numClasses, output[0].size - classStartIndex)) {
//                    val classConfidence = output[0][classStartIndex + c][maxConfidenceIndex]
//                    Log.d("✅YOLO", "클래스 $c 신뢰도: $classConfidence")
//                }
//
//                // 모델 출력의 전반적인 통계를 보기 위해 추가 정보 출력
//                var confidenceSum = 0f
//                var confidenceCount = 0
//                var nonZeroConfidences = 0
//
//                // 전체적인 신뢰도 분포 확인
//                for (i in 0 until min(1000, output[0][confidenceIndex].size)) {
//                    val conf = output[0][confidenceIndex][i]
//                    confidenceSum += conf
//                    confidenceCount++
//                    if (conf > 0.01f) nonZeroConfidences++
//                }
//
//                val avgConfidence = if (confidenceCount > 0) confidenceSum / confidenceCount else 0f
//                Log.d("✅YOLO", "신뢰도 통계 (샘플): 평균=$avgConfidence, 0.01 이상 값 수=$nonZeroConfidences")
//            }
//
//            return YoloResult.None
//        } catch (e: Exception) {
//            Log.e("✅YOLO", "결과 파싱 오류: ${e.message}")
//            e.printStackTrace()
//            return YoloResult.None
//        }
//    }
//
//    fun stop() {
//        shouldRun = false
//    }
//    fun isRunningAllowed(): Boolean = shouldRun
//
//    // 앱이 종료되거나 클래스 사용이 끝나면 모델 리소스를 정리하여 메모리 누수 방지
//    override fun close() {
//        synchronized(this) {
//            while (isRunning) {
//                Log.w("✅YOLO", "❗추론 중이라 인터프리터 닫기 대기")
//                try {
//                    Thread.sleep(10)
//                } catch (e: InterruptedException) {
//                    break
//                }
//            }
//
//            try {
//                interpreter?.close()
//                interpreter = null
//                Log.d("✅YOLO", "✅ 인터프리터 정상 해제 완료")
//            } catch (e: Exception) {
//                Log.e("✅YOLO", "리소스 해제 중 오류: ${e.message}")
//            }
//        }
//    }
//
//}
