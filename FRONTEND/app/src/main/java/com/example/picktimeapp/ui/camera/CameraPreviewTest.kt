//package com.example.picktimeapp.ui.camera
//
//import android.content.Context
//import android.util.Log
//import android.util.Size
//import androidx.camera.core.AspectRatio
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.picktimeapp.audio.AudioComm
//import com.example.picktimeapp.controller.FeedbackController
//import com.example.picktimeapp.util.CameraAnalyzerViewModel
//import com.example.picktimeapp.util.CameraFrameAnalyzerTest
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//@Composable
//fun CameraPreviewTest(
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val viewModel: CameraAnalyzerViewModel = hiltViewModel()
//    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
//
//    val cameraFrameAnalyzerTest = remember {
//        CameraFrameAnalyzerTest(
//            context = context,
//            viewModel = viewModel,
//            shouldRun = { true }
//        )
//    }
//
//    remember {
//        FeedbackController(cameraFrameAnalyzerTest)
//    }
//
//    DisposableEffect(true) {
//        onDispose {
//            cameraExecutor.shutdown()
//            AudioComm.stopAudioProcessing()
//        }
//    }
//
//    AndroidView(
//        modifier = modifier,
//        factory = { ctx ->
//            val previewView = PreviewView(ctx).apply {
//                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//            }
//
//            startCamera(
//                context = ctx,
//                previewView = previewView,
//                lifecycleOwner = lifecycleOwner,
//                cameraExecutor = cameraExecutor,
//                analyzer = cameraFrameAnalyzerTest
//            )
//
//            AudioComm.startAudioProcessing()
//            previewView
//        }
//    )
//}
//
///**
// * 실제 카메라 초기화 및 이미지 분석을 수행하는 함수
// *
// * @param context         안드로이드 Context
// * @param previewView     카메라 미리보기를 위한 PreviewView
// * @param lifecycleOwner  카메라 생명주기를 관리하는 LifecycleOwner
// * @param cameraExecutor  백그라운드에서 카메라 분석을 수행할 ExecutorService
// * @param analyzer        사용할 ImageAnalysis.Analyzer 인스턴스
// */
//private fun startCamera(
//    context: Context,
//    previewView: PreviewView,
//    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
//    cameraExecutor: ExecutorService,
//    analyzer: ImageAnalysis.Analyzer
//) {
//    val TAG = "CameraPreviewTest"
//
//    try {
//        // CameraProvider 획득 요청
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//        cameraProviderFuture.addListener({
//            try {
//                val cameraProvider = cameraProviderFuture.get()
//
//                // Preview 생성 및 설정
//                val preview = Preview.Builder()
//                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//                    .build()
//                    .also {
//                        it.setSurfaceProvider(previewView.surfaceProvider)
//                    }
//
//                // ImageAnalysis 생성, analyzer는 외부에서 전달한 인스턴스를 사용
//                val imageAnalysis = ImageAnalysis.Builder()
//                    .setTargetResolution(Size(1280, 736))
//                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                    .build()
//                    .also {
//                        it.setAnalyzer(cameraExecutor, analyzer)
//                    }
//
//                // 전면 카메라 선택
//                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//
//                // 기존 바인딩 해제 후 새로운 바인딩 설정
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    lifecycleOwner,
//                    cameraSelector,
//                    preview,
//                    imageAnalysis
//                )
//
//                Log.d(TAG, "카메라 초기화 성공")
//            } catch (e: Exception) {
//                Log.e(TAG, "카메라 바인딩 실패: ${e.message}")
//            }
//        }, ContextCompat.getMainExecutor(context))
//    } catch (e: Exception) {
//        Log.e(TAG, "카메라 초기화 실패: ${e.message}")
//    }
//}