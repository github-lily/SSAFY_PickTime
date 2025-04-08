package com.example.picktimeapp.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.example.picktimeapp.util.CameraFrameAnalyzer
import com.example.picktimeapp.util.ondevice.CameraFrameAnalyzerOndevice
//import com.example.picktimeapp.util.HandLandmarkerHelper
//import com.example.picktimeapp.util.MediapipeOverlayView
//import com.example.picktimeapp.util.YoloSegmentationHelper
//import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.Executors


@Composable
fun CameraPreviewOndevice(
    modifier: Modifier = Modifier,
    onFrameCaptured: (Bitmap) -> Unit = {}
//    onDetectionResult: (YoloResult) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val TAG = "CameraPreview"

    // 카메라 및 ML 모델 관련 리소스
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
//    val yoloHelper = remember { YoloSegmentationHelper(context) }

    // Composable이 제거될 때 리소스 해제
    DisposableEffect(key1 = true) {
        onDispose {
            Log.d(TAG, "카메라 리소스 해제")

//            // ✅ 추론 중지 먼저 요청
//            yoloHelper.stop()
//
//            // ✅ 약간의 딜레이 (아직 남은 프레임 처리 대기)
//            Thread.sleep(100)
//
//            // ✅ 카메라 쓰레드 종료 및 모델 해제
            cameraExecutor.shutdown()
//            yoloHelper.close()
        }
    }

    // 미리보기 화면을 띄우는 부분
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

//            val overlayView = MediapipeOverlayView(ctx, null)
//
//            val handLandmarkerHelper = HandLandmarkerHelper(
//                context = ctx,
//                runningMode = RunningMode.LIVE_STREAM,
//                handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
//                    override fun onError(error: String, errorCode: Int) {
//                        Log.e("HandLandmarkerHelper", "에러: $error")
//                    }
//
//                    override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
//                        if (resultBundle.results.isNotEmpty()) {
//                            overlayView.setResults(
//                                handLandmarkerResults = resultBundle.results[0],
//                                imageHeight = resultBundle.inputImageHeight,
//                                imageWidth = resultBundle.inputImageWidth,
//                                runningMode = RunningMode.LIVE_STREAM
//                            )
//                        }
//                    }
//                }
//            )

            val container = android.widget.FrameLayout(ctx).apply {
                addView(previewView)
//                addView(overlayView)
            }

            startCamera(
                context = ctx,
                previewView = previewView,
//                overlayView = overlayView,
//                handLandmarkerHelper = handLandmarkerHelper, // 👈 추가!
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                onFrameCaptured = onFrameCaptured,
//                yoloHelper = yoloHelper,
//                onDetectionResult = onDetectionResult,
            )

            container
        }

//        factory = { ctx: Context ->
//            val previewView = PreviewView(ctx).apply {
//                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//            }
//
//            val overlayView = MediapipeOverlayView(ctx, null)
//
//            val container = android.widget.FrameLayout(ctx).apply {
//                addView(previewView)
//                addView(overlayView)
//            }
//
//            startCamera(
//                context = ctx,
//                previewView = previewView,
//                overlayView = overlayView, // mediapipe
//                lifecycleOwner = lifecycleOwner,
//                cameraExecutor = cameraExecutor,
//                yoloHelper = yoloHelper,
//                onDetectionResult = onDetectionResult
//            )
//
//            container // AndroidView가 렌더링할 뷰로 return
//        }
    )

}

private fun startCamera(
    context: Context,
    previewView: PreviewView,
//    overlayView: MediapipeOverlayView,
//    handLandmarkerHelper: HandLandmarkerHelper,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: java.util.concurrent.ExecutorService,
    onFrameCaptured: (Bitmap) -> Unit,
//    yoloHelper: YoloSegmentationHelper,
//    onDetectionResult: (YoloResult) -> Unit
) {
    val TAG = "CameraPreview"

    try {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                // 카메라 연결 도우미 객체 불러오기
                val cameraProvider = cameraProviderFuture.get()

                // 카메라 영상을 화면에 표시해주는 preview 객체 생성
                val preview = Preview.Builder()
//                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // 실시간 프레임 분석 설정
                val imageAnalysis = ImageAnalysis.Builder()
                    // 원본 해상도로 분석 - 기기에 따라 부하가 클 수 있음
                    .setTargetResolution(Size(1280, 736)) // 또는 디바이스 화면 해상도에 맞게 조정
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 최신 프레임만 분석
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            CameraFrameAnalyzerOndevice(
                                onResult = { bitmap, timestamp ->
                                    onFrameCaptured(bitmap.copy(bitmap.config, true))
                                }
                            )
                        )
                    }

//                                onResult = { bitmap, timestamp  ->
//                                    onFrameCaptured(bitmap.copy(bitmap.config, true)) // bitmap 복사본 넘기기
//                                    try {
//                                        val result = yoloHelper.runInference(bitmap)
//                                        onDetectionResult(result)
//                                    } catch (e: Exception) {
//                                        Log.e(TAG, "추론 중 오류: ${e.message}")
//                                    }
//                                },
//                                shouldRun = { yoloHelper.isRunningAllowed() },
//                                handLandmarkerHelper = handLandmarkerHelper,
//                                overlayView = overlayView,
//                                isFrontCamera = false
//                            )
//                            CameraFrameAnalyzer(
//                                onResult = { bitmap, timestamp  ->
//                                    try {
//                                        // YoloResult 객체를 직접 반환하도록 수정
//                                        val result = yoloHelper.runInference(bitmap)
//                                        onDetectionResult(result)
//                                    } catch (e: Exception) {
//                                        Log.e(TAG, "추론 중 오류: ${e.message}")
//                                    }
//                                },
//                                shouldRun = { yoloHelper.isRunningAllowed() }
//                            )


                // 전면 또는 후면 카메라 선택
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // 필요에 따라 변경

                // 기존 바인딩 해제 후 새로 바인딩
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                Log.d(TAG, "카메라 초기화 성공")
            } catch (e: Exception) {
                Log.e(TAG, "카메라 바인딩 실패: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    } catch (e: Exception) {
        Log.e(TAG, "카메라 초기화 실패: ${e.message}")
    }
}


