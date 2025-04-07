package com.example.picktimeapp.data.model
// 코드 확인하는 JSON 파일용 데이터 클래스

data class ChordFingeringData(
    val chord: String,
    val chordFingering: ChordFingeringDetailJson
)

data class ChordFingeringDetailJson(
    val positions: List<FingerPlacementJson>,
    val openStrings: List<Int>,
    val muteStrings: List<Int>
)

data class FingerPlacementJson(
    val finger: Int,
    val fret: Int,
    val strings: List<Int>
)