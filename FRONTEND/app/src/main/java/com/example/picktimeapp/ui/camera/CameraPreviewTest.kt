package com.example.picktimeapp.ui.camera

import android.content.Context
import android.provider.MediaStore.Audio
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
import com.example.picktimeapp.audio.AudioComm
import com.example.picktimeapp.util.CameraFrameAnalyzer
import java.util.concurrent.Executors

/**
 * Composable 함수로 카메라 미리보기와
 *
 * @param modifier UI 수정에 사용되는 Modifier
 */
@Composable
fun CameraPreviewTest(
    modifier: Modifier = Modifier
) {
    // 현재 Context와 LifecycleOwner를 가져옵니다.
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val TAG = "CameraPreview"

    // 카메라 작업을 위한 별도의 단일 스레드 Executor를 생성합니다.
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }


    // Composable이 화면에서 제거될 때 리소스를 정리하기 위한 DisposableEffect 사용
    DisposableEffect(key1 = true) {
        onDispose {
            Log.d(TAG, "카메라 리소스 해제")

            // 2. 남아있는 프레임 처리를 위한 잠시 대기 (100ms)
            Thread.sleep(100)

            // 3. 카메라 스레드 종료 및 YOLO 헬퍼의 리소스 해제
            cameraExecutor.shutdown()
            AudioComm.stopAudioProcessing()
        }
    }

    // AndroidView를 통해 기존 안드로이드 View(PreviewView)를 Compose UI에 통합
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            // PreviewView 생성 및 설정 (호환성 모드 사용)
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            // 카메라를 시작하고 미리보기 및 이미지 분석 기능을 연결
            startCamera(
                context = ctx,
                previewView = previewView,
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor
            )
            AudioComm.startAudioProcessing()
            // 구성된 PreviewView를 반환하여 화면에 표시
            previewView
        }
    )
}

/**
 * 실제 카메라 초기화 및 이미지 분석을 수행하는 함수
 *
 * @param context 안드로이드 Context
 * @param previewView 카메라 미리보기를 위한 PreviewView
 * @param lifecycleOwner 카메라 생명주기를 관리하는 LifecycleOwner
 * @param cameraExecutor 백그라운드에서 카메라 분석을 수행할 ExecutorService
 */
private fun startCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: java.util.concurrent.ExecutorService
) {
    val TAG = "CameraPreview"

    try {
        // ProcessCameraProvider를 이용하여 카메라 기능을 제공받기 위한 Future 객체를 요청
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        // Future가 완료되면 실행할 Listener 등록
        cameraProviderFuture.addListener({
            try {
                // 실제 카메라 기능에 접근하기 위한 CameraProvider 객체 획득
                val cameraProvider = cameraProviderFuture.get()

                // 카메라 미리보기를 위한 Preview 객체 생성
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 16:9 화면 비율 설정
                    .build()
                    .also {
                        // PreviewView의 surfaceProvider와 연결해 화면에 출력
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // 이미지 분석을 위한 ImageAnalysis 객체 생성
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 736)) // 분석에 사용할 해상도 설정 (디바이스에 따라 조정 가능)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 최신 프레임만 처리하도록 설정
                    .build()
                    .also {
                        // 이미지 분석기를 설정하여 매 프레임마다 작업 실행
                        it.setAnalyzer(
                            cameraExecutor,
                            CameraFrameAnalyzer(
                                onResult = { bitmap ->
                                    try {
                                            // 작업 코드
                                    } catch (e: Exception) {
                                        Log.e(TAG, "추론 중 오류: ${e.message}")
                                    }
                                },
                                // 추론 실행 여부를 결정하는 조건 (예: 중복 실행 방지)
                                shouldRun = { false }
                            )
                        )
                    }

                // 전면 카메라 선택 (필요에 따라 CameraSelector.DEFAULT_BACK_CAMERA로 변경 가능)
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                // 이전에 바인딩된 카메라 리소스가 있다면 모두 해제하고 새로운 바인딩 설정
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                Log.d(TAG, "카메라 초기화 성공")
            } catch (e: Exception) {
                // 카메라 바인딩 과정에서 발생한 예외 처리
                Log.e(TAG, "카메라 바인딩 실패: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    } catch (e: Exception) {
        // ProcessCameraProvider 초기화 실패 시 예외 처리
        Log.e(TAG, "카메라 초기화 실패: ${e.message}")
    }
}
