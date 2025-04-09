package com.example.picktimeapp.ui.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.network.YoloPositionResponse
import com.example.picktimeapp.network.YoloServerApi
import com.example.picktimeapp.util.Utils
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
        onResult: (YoloPositionResponse) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val imagePart = Utils.bitmapToMultipart(bitmap)
                val response = yoloServerApi.sendFrame(imagePart)

                if (response.detectionDone == true) {
                    positionDetected.value = true
                    Log.d("AI", "Position 인식 성공")
                } else {
                    Log.e("AI", "Position 인식 실패")
                }

                onResult(response)

            } catch (e: Exception) {
                Log.e("AI", "통신 오류: ${e.message}")
            }
        }
    }

}
