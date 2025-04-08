package com.example.picktimeapp.ui.camera

import android.content.Context
import android.graphics.Bitmap
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picktimeapp.audio.AudioComm
import com.example.picktimeapp.controller.FeedbackController
import com.example.picktimeapp.util.CameraAnalyzerViewModel
import com.example.picktimeapp.util.CameraFrameAnalyzer
import com.example.picktimeapp.util.ChordCheckViewModel
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

/**
 * Composable 함수로 카메라 미리보기와
 *
 * @param modifier UI 수정에 사용되는 Modifier
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    viewModel: ChordCheckViewModel,
    onFrameCaptured: (Bitmap) -> Unit = {}
) {
    // 현재 Context와 LifecycleOwner를 가져옵니다.
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraViewModel: CameraAnalyzerViewModel = hiltViewModel()
    val TAG = "CameraPreview"

    // 카메라 백그라운드 처리를 위한 Executor 생성
    // 카메라 작업을 위한 별도의 단일 스레드 Executor 생성
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }


    // 단일 Analyzer(분석기) 인스턴스 생성 및 상태 유지(FeedbackController와 함께 공유)
    val cameraAnalyzer = remember {
        CameraFrameAnalyzer(
            context = context,

            // 📌 실시간 1장 전송용 콜백 (detection_done == false 일 때만 호출됨)
            onResult = { bitmap ->
                viewModel.sendSingleFrame(bitmap)
            },
            viewModel = cameraViewModel,
            // 📌 1장 전송 여부 판단 조건 (detectionDone == true면, shouldRun == false가 되어 실시간 전송 중단)
            shouldRun = { viewModel.detectionDone.value == false }
        ).apply {
            // ✅ 10장 수집 완료 시 호출될 콜백 (연주 감지 후)
            onCaptureComplete = { frames ->
                viewModel.sendFrameList(frames)
            }
        }
    }

    // FeedbackController 생성: AudioComm 이벤트가 발생하면 cameraFrameAnalyzer.startCapture() 호출
    remember {
        FeedbackController(cameraAnalyzer)
    }

    // Composable이 제거될 때 리소스 정리 (카메라, 오디오 처리 등)
    DisposableEffect(key1 = true) {
        onDispose {
            Log.d(TAG, "카메라 리소스 해제")
            Thread.sleep(100)
            cameraExecutor.shutdown()
            AudioComm.stopAudioProcessing()
        }
    }

    // ✅ 실제 Android 카메라 뷰 구성
    // AndroidView를 통해 PreviewView를 Compose UI에 통합
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            // PreviewView 생성 및 설정 (호환성 모드 사용)
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            // 카메라 초기화 시 Analyzer 인스턴스로 cameraFrameAnalyzerTest를 전달
            startCamera(
                context = ctx,
                previewView = previewView,
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                analyzer = cameraAnalyzer
            )
            // 오디오 분석 시작
            AudioComm.startAudioProcessing()

            // 구성된 PreviewView를 반환하여 화면에 표시
            previewView
        }
    )
}

/**
 * 실제 카메라 초기화 및 이미지 분석을 수행하는 함수
 *
 * @param context         안드로이드 Context
 * @param previewView     카메라 미리보기를 위한 PreviewView
 * @param lifecycleOwner  카메라 생명주기를 관리하는 LifecycleOwner
 * @param cameraExecutor  백그라운드에서 카메라 분석을 수행할 ExecutorService
 * @param analyzer        사용할 ImageAnalysis.Analyzer 인스턴스
 */
private fun startCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: ExecutorService,
    analyzer: ImageAnalysis.Analyzer
) {
    val TAG = "CameraPreviewTest"

    try {
        // CameraProvider 획득 요청
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Preview 생성 및 설정
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // ImageAnalysis 생성, analyzer는 외부에서 전달한 인스턴스를 사용
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 736))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        // 외부에서 받은 analyzer를 카메라에 연결
                        it.setAnalyzer(cameraExecutor, analyzer)
                    }

                // 전면 카메라 선택
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                // 기존 바인딩 해제 후 새로운 바인딩 설정
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
