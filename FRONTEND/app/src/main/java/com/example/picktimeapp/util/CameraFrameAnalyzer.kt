package com.example.picktimeapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

class CameraFrameAnalyzer(
    private val onResult: (Bitmap) -> Unit,
    private val shouldRun: () -> Boolean
) : ImageAnalysis.Analyzer {

    private var lastInferenceTime = 0L
    private val inferenceInterval = 500L // 추론 간격 (밀리초)
    private val TAG = "CameraFrameAnalyzer"

    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        // ✅ 추론 중지 요청되면 건너뜀
        if (!shouldRun()) {
            image.close()
            return
        }

        // 추론 간격 제한
        if (currentTime - lastInferenceTime < inferenceInterval) {
            image.close()
            return
        }

        try {
            // 이미지 변환 시도
            val bitmap = imageProxyToBitmap(image)
            if (bitmap != null) {

                onResult(bitmap)

                lastInferenceTime = currentTime
                bitmap.recycle() // 원본 비트맵 메모리 해제
            } else {
                Log.e(TAG, "비트맵 변환 실패")
            }
        } catch (e: Exception) {
            Log.e(TAG, "이미지 처리 중 오류: ${e.message}")
        } finally {
            image.close() // 항상 리소스 해제
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
        // U와 V 플레인은 Y 플레인보다 작을 수 있음
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

        // Bitmap으로 변환
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
