//package com.example.picktimeapp.util
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Matrix
//import android.media.MediaMetadataRetriever
//import android.net.Uri
//import android.os.SystemClock
//import android.util.Log
//import androidx.annotation.VisibleForTesting
//import androidx.camera.core.ImageProxy
//import com.google.mediapipe.framework.image.BitmapImageBuilder
//import com.google.mediapipe.framework.image.MPImage
//import com.google.mediapipe.tasks.core.BaseOptions
//import com.google.mediapipe.tasks.core.Delegate
//import com.google.mediapipe.tasks.vision.core.RunningMode
//import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
//import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
//
//class HandLandmarkerHelper(
//    var minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
//    var minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
//    var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
//    var maxNumHands: Int = DEFAULT_NUM_HANDS,
//    var currentDelegate: Int = DELEGATE_CPU,
//    var runningMode: RunningMode = RunningMode.IMAGE,
//    val context: Context,
//    // this listener is only used when running in RunningMode.LIVE_STREAM
//    val handLandmarkerHelperListener: LandmarkerListener? = null
//) {
//
//    // For this example this needs to be a var so it can be reset on changes.
//    // If the Hand Landmarker will not change, a lazy val would be preferable.
//    private var handLandmarker: HandLandmarker? = null
//
//    init {
//        setupHandLandmarker()
//    }
//
//    fun clearHandLandmarker() {
//        handLandmarker?.close()
//        handLandmarker = null
//    }
//
//    // Return running status of HandLandmarkerHelper
//    fun isClose(): Boolean {
//        return handLandmarker == null
//    }
//
//    // Initialize the Hand landmarker using current settings on the
//    // thread that is using it. CPU can be used with Landmarker
//    // that are created on the main thread and used on a background thread, but
//    // the GPU delegate needs to be used on the thread that initialized the
//    // Landmarker
//    fun setupHandLandmarker() {
//        // Set general hand landmarker options
//        val baseOptionBuilder = BaseOptions.builder()
//
//        // Use the specified hardware for running the model. Default to CPU
//        when (currentDelegate) {
//            DELEGATE_CPU -> {
//                baseOptionBuilder.setDelegate(Delegate.CPU)
//            }
//            DELEGATE_GPU -> {
//                baseOptionBuilder.setDelegate(Delegate.GPU)
//            }
//        }
//
//        baseOptionBuilder.setModelAssetPath(MP_HAND_LANDMARKER_TASK)
//
//        // Check if runningMode is consistent with handLandmarkerHelperListener
//        when (runningMode) {
//            RunningMode.LIVE_STREAM -> {
//                if (handLandmarkerHelperListener == null) {
//                    throw IllegalStateException(
//                        "handLandmarkerHelperListener must be set when runningMode is LIVE_STREAM."
//                    )
//                }
//            }
//            else -> {
//                // no-op
//            }
//        }
//
//        try {
//            val baseOptions = baseOptionBuilder.build()
//            // Create an option builder with base options and specific
//            // options only use for Hand Landmarker.
//            val optionsBuilder =
//                HandLandmarker.HandLandmarkerOptions.builder()
//                    .setBaseOptions(baseOptions)
//                    .setMinHandDetectionConfidence(minHandDetectionConfidence)
//                    .setMinTrackingConfidence(minHandTrackingConfidence)
//                    .setMinHandPresenceConfidence(minHandPresenceConfidence)
//                    .setNumHands(maxNumHands)
//                    .setRunningMode(runningMode)
//
//            // The ResultListener and ErrorListener only use for LIVE_STREAM mode.
//            if (runningMode == RunningMode.LIVE_STREAM) {
//                optionsBuilder
//                    .setResultListener(this::returnLivestreamResult)
//                    .setErrorListener(this::returnLivestreamError)
//            }
//
//            val options = optionsBuilder.build()
//            handLandmarker =
//                HandLandmarker.createFromOptions(context, options)
//        } catch (e: IllegalStateException) {
//            handLandmarkerHelperListener?.onError(
//                "Hand Landmarker failed to initialize. See error logs for " +
//                        "details"
//            )
//            Log.e(
//                TAG, "MediaPipe failed to load the task with error: " + e
//                    .message
//            )
//        } catch (e: RuntimeException) {
//            // This occurs if the model being used does not support GPU
//            handLandmarkerHelperListener?.onError(
//                "Hand Landmarker failed to initialize. See error logs for " +
//                        "details", GPU_ERROR
//            )
//            Log.e(
//                TAG,
//                "Image classifier failed to load model with error: " + e.message
//            )
//        }
//    }
//
//    // Convert the ImageProxy to MP Image and feed it to HandlandmakerHelper.
//    fun detectLiveStream(
//        imageProxy: ImageProxy,
//        isFrontCamera: Boolean
//    ) {
//        if (runningMode != RunningMode.LIVE_STREAM) {
//            throw IllegalArgumentException(
//                "Attempting to call detectLiveStream" +
//                        " while not using RunningMode.LIVE_STREAM"
//            )
//        }
//        val frameTime = SystemClock.uptimeMillis()
//
//        // Copy out RGB bits from the frame to a bitmap buffer
//        val bitmapBuffer =
//            Bitmap.createBitmap(
//                imageProxy.width,
//                imageProxy.height,
//                Bitmap.Config.ARGB_8888
//            )
//        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
//        imageProxy.close()
//
//        val matrix = Matrix().apply {
//            // Rotate the frame received from the camera to be in the same direction as it'll be shown
//            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
//
//            // flip image if user use front camera
//            if (isFrontCamera) {
//                postScale(
//                    -1f,
//                    1f,
//                    imageProxy.width.toFloat(),
//                    imageProxy.height.toFloat()
//                )
//            }
//        }
//        val rotatedBitmap = Bitmap.createBitmap(
//            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
//            matrix, true
//        )
//
//        // Convert the input Bitmap object to an MPImage object to run inference
//        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
//
//        detectAsync(mpImage, frameTime)
//    }
//
//    // Run hand hand landmark using MediaPipe Hand Landmarker API
//    @VisibleForTesting
//    fun detectAsync(mpImage: MPImage, frameTime: Long) {
//        handLandmarker?.detectAsync(mpImage, frameTime)
//        // As we're using running mode LIVE_STREAM, the landmark result will
//        // be returned in returnLivestreamResult function
//    }
//
//
//
//    // Return the landmark result to this HandLandmarkerHelper's caller
//    private fun returnLivestreamResult(
//        result: HandLandmarkerResult,
//        input: MPImage
//    ) {
//        val finishTimeMs = SystemClock.uptimeMillis()
//        val inferenceTime = finishTimeMs - result.timestampMs()
//
//        handLandmarkerHelperListener?.onResults(
//            ResultBundle(
//                listOf(result),
//                inferenceTime,
//                input.height,
//                input.width
//            )
//        )
//    }
//
//    // Return errors thrown during detection to this HandLandmarkerHelper's
//    // caller
//    private fun returnLivestreamError(error: RuntimeException) {
//        handLandmarkerHelperListener?.onError(
//            error.message ?: "An unknown error has occurred"
//        )
//    }
//
//    companion object {
//        const val TAG = "HandLandmarkerHelper"
//        private const val MP_HAND_LANDMARKER_TASK = "hand_landmarker.task"
//
//        const val DELEGATE_CPU = 0
//        const val DELEGATE_GPU = 1
//        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
//        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
//        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
//        const val DEFAULT_NUM_HANDS = 1
//        const val OTHER_ERROR = 0
//        const val GPU_ERROR = 1
//    }
//
//    data class ResultBundle(
//        val results: List<HandLandmarkerResult>,
//        val inferenceTime: Long,
//        val inputImageHeight: Int,
//        val inputImageWidth: Int,
//    )
//
//    interface LandmarkerListener {
//        fun onError(error: String, errorCode: Int = OTHER_ERROR)
//        fun onResults(resultBundle: ResultBundle)
//    }
//
//    fun detectLiveStream(bitmap: Bitmap, isFrontCamera: Boolean) {
//        if (runningMode != RunningMode.LIVE_STREAM) {
//            throw IllegalArgumentException("detectLiveStream() requires LIVE_STREAM mode")
//        }
//
//        val frameTime = SystemClock.uptimeMillis()
//
//        val matrix = Matrix().apply {
//            if (isFrontCamera) {
//                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
//            }
//        }
//        val rotatedBitmap = Bitmap.createBitmap(
//            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
//        )
//
//        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
//        detectAsync(mpImage, frameTime)
//    }
//
//}
//
//
//
//
////
////import android.content.Context
////import android.graphics.Bitmap
////import com.google.mediapipe.tasks.vision.core.RunningMode
////import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
////import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
////import com.google.mediapipe.framework.image.BitmapImageBuilder
////import com.google.mediapipe.tasks.core.BaseOptions
////
////
////class HandLandmarkerHelper(
////    context: Context,
////    private val onResult: (HandLandmarkerResult) -> Unit,
////    private val onError: (RuntimeException) -> Unit
////) {
////
////    private val handLandmarker: HandLandmarker
////
////    init {
////        val baseOptions = BaseOptions.builder()
////            .setModelAssetPath("hand_landmarker.task")
////            .build()
////
////        val options = HandLandmarker.HandLandmarkerOptions.builder()
////            .setBaseOptions(baseOptions)
////            .setMinHandDetectionConfidence(0.5f)
////            .setMinTrackingConfidence(0.5f)
////            .setMinHandPresenceConfidence(0.5f)
////            .setNumHands(2)
////            // mediapipe 내부 비동기 처리 + 콜백으로 결과 전달
////            .setRunningMode(RunningMode.LIVE_STREAM)
////            .setResultListener { result, _ -> onResult(result) }    // 프레임 분석 결과 콜백
////            .setErrorListener { e -> onError(e) }                   // 에러 발생 시 콜백
////            .build()
////
////        handLandmarker = HandLandmarker.createFromOptions(context, options)
////    }
////
////
////    // timestampMs: currentTime(점점 증가하는 값)
////    fun sendLivestreamFrame(bitmap: Bitmap, timestampMs: Long) {
////        val mpImage = BitmapImageBuilder(bitmap).build()    // Bitmap 프레임 준비
////        handLandmarker.detectAsync(mpImage, timestampMs)    // 이미지 + timestamp 수동 전달
////    }
////}
