//package com.example.picktimeapp.util
//
//import android.content.Context
//import com.example.picktimeapp.data.model.FingerPlacementJson
//import com.google.gson.Gson
//import com.example.picktimeapp.data.model.ChordFingeringData
//
//object ChordChecker {
//
//    fun isChordCorrect(
//        aiPositions: List<FingerPlacementJson>,
//        targetChord: String,
//        context: Context
//    ): Boolean {
//        val chordList = JsonUtils.loadChordFingeringData(context)
//        val target = chordList.find { it.chord == targetChord } ?: return false
//        val expectedPositions = target.chordFingering.positions
//
//        if (aiPositions.size != expectedPositions.size) return false
//
//        val sortedAi = aiPositions.sortedBy { it.finger }
//        val sortedExpected = expectedPositions.sortedBy { it.finger }
//
//        for (i in sortedAi.indices) {
//            val ai = sortedAi[i]
//            val expected = sortedExpected[i]
//
//            if (
//                ai.finger != expected.finger ||
//                ai.fret != expected.fret ||
//                ai.strings.sorted() != expected.strings.sorted()
//            ) {
//                return false
//            }
//        }
//ㅅ
//        return true
//    }
//}