package com.example.picktimeapp.ui.camera

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.AspectRatio
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
import com.example.picktimeapp.data.model.YoloResult
import com.example.picktimeapp.util.CameraFrameAnalyzer
import com.example.picktimeapp.util.YoloSegmentationHelper
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onDetectionResult: (YoloResult) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val TAG = "CameraPreview"

    // 카메라 및 ML 모델 관련 리소스
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val yoloHelper = remember { YoloSegmentationHelper(context) }

    // Composable이 제거될 때 리소스 해제
    DisposableEffect(key1 = true) {
        onDispose {
            Log.d(TAG, "카메라 리소스 해제")

            // ✅ 추론 중지 먼저 요청
            yoloHelper.stop()

            // ✅ 약간의 딜레이 (아직 남은 프레임 처리 대기)
            Thread.sleep(100)

            // ✅ 카메라 쓰레드 종료 및 모델 해제
            cameraExecutor.shutdown()
            yoloHelper.close()
        }
    }

    // 미리보기 화면을 띄우는 부분
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            startCamera(
                context = ctx,
                previewView = previewView,
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                yoloHelper = yoloHelper,
                onDetectionResult = onDetectionResult
            )

            previewView
        }
    )

}

private fun startCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: java.util.concurrent.ExecutorService,
    yoloHelper: YoloSegmentationHelper,
    onDetectionResult: (YoloResult) -> Unit
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
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
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
                            CameraFrameAnalyzer(
                                onResult = { bitmap ->
                                    try {
                                        // YoloResult 객체를 직접 반환하도록 수정
                                        val result = yoloHelper.runInference(bitmap)
                                        onDetectionResult(result)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "추론 중 오류: ${e.message}")
                                    }
                                },
                                shouldRun = { yoloHelper.isRunningAllowed() }
                            )
                        )
                    }

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