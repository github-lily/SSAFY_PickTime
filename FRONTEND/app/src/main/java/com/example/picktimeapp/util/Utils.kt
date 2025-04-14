package com.example.picktimeapp.util
// 기타 운지 맞는지 틀린지 json과 불러오는 파일


import android.content.Context
import android.graphics.Bitmap
import com.example.picktimeapp.data.model.ChordFingeringData
import com.example.picktimeapp.data.model.FingerPositionData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream


object Utils {
    fun loadStandardChordMap(context: Context): Map<String, Map<String, FingerPositionData>> {
        val assetManager = context.assets
        val inputStream = assetManager.open("guitar_chord_fingerings_standard.json")
        val json = inputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<List<ChordFingeringData>>() {}.type
        val chordList: List<ChordFingeringData> = gson.fromJson(json, type)

        // 변환: List → Map<String, Map<String, FingerPosition>>
        val chordMap = mutableMapOf<String, Map<String, FingerPositionData>>()

        for (item in chordList) {
            val fingerMap = mutableMapOf<String, FingerPositionData>()
            for (pos in item.chordFingering.positions) {
                for (string in pos.strings) {
                    fingerMap[pos.finger.toString()] = FingerPositionData(
                        fretboard = pos.fret,
                        string = string
                    )
                }
            }
            chordMap[item.chord] = fingerMap
        }

        return chordMap
    }

    // 1장 변환
    // Bitmap → Multipart 변환 확장 함수
    fun bitmapToMultipart(bitmap: Bitmap, name: String = "frame.jpg"): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val requestBody = stream.toByteArray()
            .toRequestBody("image/jpeg".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData("file", name, requestBody)
    }

    // 10개 프레임 변환
    fun bitmapListToMultipartParts(
        bitmaps: List<Bitmap>,
        baseName: String = "frame"
    ): List<MultipartBody.Part> {
        return bitmaps.mapIndexed { index, bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            val requestBody = stream.toByteArray()
                .toRequestBody("image/jpeg".toMediaTypeOrNull())

            MultipartBody.Part.createFormData(
                name = "files", // 서버에서 image_0, image_1... 로 받게
                filename = "${baseName}_$index.jpg",
                body = requestBody
            )
        }
    }


}
