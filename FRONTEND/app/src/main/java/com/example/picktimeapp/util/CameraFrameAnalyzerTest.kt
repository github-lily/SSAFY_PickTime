package com.example.picktimeapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.picktimeapp.network.ChordDetectApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CameraFrameAnalyzerTest(
    private val context: Context,
    private val viewModel: CameraAnalyzerViewModel,
    private val shouldRun: () -> Boolean
) : ImageAnalysis.Analyzer {

    private var isCapturing = false
    private var frameCount = 0
    private val targetFrameCount = 10
    private val capturedBitmaps = mutableListOf<Bitmap>()
    private val TAG = "CameraFrameAnalyzer"

    init {
        // 클래스 초기화 시 sessionId 체크 후 없으면 요청
        CoroutineScope(Dispatchers.IO).launch {
            val sessionId = getSessionId(context)
            Log.d(TAG, "초기 sessionId: $sessionId")
            if (sessionId.isNullOrBlank()) {
                Log.d(TAG, "세션 없음 → 서버에 요청 시작")
                viewModel.requestSessionIdAndSave(context)
            } else {
                Log.d(TAG, "이미 세션 있음: $sessionId")
            }
        }
    }

    fun startCapture() {
        isCapturing = true
        frameCount = 0
        capturedBitmaps.clear()
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (isCapturing && frameCount < targetFrameCount) {
            val bitmap = imageProxyToBitmap(imageProxy) ?: run {
                imageProxy.close()
                return
            }
            //saveBitmapToFile(bitmap, "REcapture_frame_${frameCount}.jpg", context)
            capturedBitmaps.add(bitmap)
            frameCount++

            if (frameCount == targetFrameCount) {
                isCapturing = false

                // 👉 여기서 ViewModel로 분석 요청
                // 한 번에 묶어서 요청
                capturedBitmaps.forEach {
                    viewModel.analyzeFrame(it)
                }
            }
        }
        imageProxy.close()
    }

    // 수정된 Bitmap을 파일로 저장하는 함수 (앱 전용 외부 저장소 사용)
    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String, context: Context) {
        try {
            // 앱 전용 외부 저장소의 Pictures 디렉토리 하위에 "testImg" 폴더 생성
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "testImg")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            val file = File(appDir, fileName)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            Log.d(TAG, "Saved bitmap to: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        return try {
            when (image.format) {
                ImageFormat.YUV_420_888 -> {
                    yuv420ToBitmap(image)
                }
                PixelFormat.RGBA_8888 -> {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                else -> {
                    Log.e(TAG, "지원하지 않는 이미지 포맷: ${image.format}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "이미지 변환 오류: ${e.message}")
            null
        }
    }

    private fun yuv420ToBitmap(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y 플레인 복사
        yBuffer.get(nv21, 0, ySize)

        // UV 플레인 인터리빙
        val uvPixelStride = image.planes[1].pixelStride
        val uvRowStride = image.planes[1].rowStride
        val uvWidth = image.width / 2
        val uvHeight = image.height / 2

        for (row in 0 until uvHeight) {
            for (col in 0 until uvWidth) {
                val uvIndex = col * uvPixelStride + row * uvRowStride
                val nv21Index = ySize + row * uvWidth * 2 + col * 2

                if (uvIndex < uBuffer.limit()) {
                    nv21[nv21Index] = vBuffer.get(uvIndex)
                }
                if (uvIndex + 1 < vBuffer.limit()) {
                    nv21[nv21Index + 1] = uBuffer.get(uvIndex)
                }
            }
        }

        // YuvImage로 변환
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val jpegData = out.toByteArray()
        var bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)

        // 회전 보정 (필요한 경우)
        if (image.imageInfo.rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        return bitmap
    }
}
