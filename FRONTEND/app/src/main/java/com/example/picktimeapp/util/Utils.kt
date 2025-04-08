package com.example.picktimeapp.util
// 기타 운지 맞는지 틀린지 json과 불러오는 파일


import android.content.Context
import android.graphics.Bitmap
import com.example.picktimeapp.data.model.ChordFingeringData
import com.example.picktimeapp.data.model.FingerPosition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream


object Utils {
    fun loadStandardChordMap(context: Context): Map<String, Map<String, FingerPosition>> {
        val assetManager = context.assets
        val inputStream = assetManager.open("guitar_chord_fingerings_standard.json")
        val json = inputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<List<ChordFingeringData>>() {}.type
        val chordList: List<ChordFingeringData> = gson.fromJson(json, type)

        // 변환: List → Map<String, Map<String, FingerPosition>>
        val chordMap = mutableMapOf<String, Map<String, FingerPosition>>()

        for (item in chordList) {
            val fingerMap = mutableMapOf<String, FingerPosition>()
            for (pos in item.chordFingering.positions) {
                for (string in pos.strings) {
                    fingerMap[pos.finger.toString()] = FingerPosition(
                        fretboard = pos.fret,
                        string = string
                    )
                }
            }
            chordMap[item.chord] = fingerMap
        }

        return chordMap
    }

    // Bitmap → Multipart 변환 확장 함수
    fun bitmapToMultipart(bitmap: Bitmap, name: String = "frame.jpg"): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val requestBody = stream.toByteArray()
            .toRequestBody("image/jpeg".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData("image", name, requestBody)
    }

}
