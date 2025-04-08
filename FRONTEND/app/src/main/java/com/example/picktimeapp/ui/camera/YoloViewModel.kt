package com.example.picktimeapp.ui.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.DetectionResponse
import com.example.picktimeapp.network.YoloServerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@HiltViewModel
class YoloViewModel @Inject constructor(
    private val yoloServerApi: YoloServerApi
) : ViewModel() {

    val positionDetected = mutableStateOf(false)

    fun sendFrameToServer(
        bitmap: Bitmap,
        onResult: (DetectionResponse) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val imagePart = bitmapToMultipart(bitmap)
                val response = yoloServerApi.sendFrame(imagePart)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.position == true) {
                        positionDetected.value = true
                        Log.d("AI", "Position 인식 성공")
                    }
                } else {
                    Log.e("AI", "서버 오류: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("AI", "통신 오류: ${e.message}")
            }
        }
    }
}


// 서버 통신할 수 있는 Multipart 방식으로 변환
private fun bitmapToMultipart(bitmap: Bitmap, name: String = "frame.jpg"): MultipartBody.Part {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    val requestBody = stream.toByteArray()
        .toRequestBody("image/jpeg".toMediaTypeOrNull())

    return MultipartBody.Part.createFormData("image", name, requestBody)
}
