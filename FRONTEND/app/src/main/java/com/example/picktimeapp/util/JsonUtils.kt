package com.example.picktimeapp.util

import android.content.Context
import com.example.picktimeapp.data.model.ChordFingeringData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object JsonUtils {

    fun loadChordFingeringData(context: Context): List<ChordFingeringData> {
        val assetManager = context.assets
        val inputStream = assetManager.open("chords/chords_data.json")
        val reader = InputStreamReader(inputStream, "UTF-8")

        val listType = object : TypeToken<List<ChordFingeringData>>() {}.type
        return Gson().fromJson(reader, listType)
    }
}

//사용 예시
//val context = LocalContext.current
//val chordData = JsonUtils.loadChordFingeringData(context)
//Log.d("ChordData", "불러온 코드 수: ${chordData.size}")