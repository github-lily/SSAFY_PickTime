package com.example.picktimeapp.util
// 기타 운지 맞는지 틀린지 json과 불러오는 파일


import android.content.Context
import com.example.picktimeapp.data.model.ChordFingeringData
import com.example.picktimeapp.data.model.ChordPosition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FingerPosition(val fretboard: Int, val string: Int)

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
}
