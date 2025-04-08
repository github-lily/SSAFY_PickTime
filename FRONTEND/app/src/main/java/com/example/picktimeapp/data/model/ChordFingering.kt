package com.example.picktimeapp.data.model
// 손 운지 확인 기준 JSON 파일용 데이터 클래스

data class ChordFingeringData(
    val chord: String,
    val chordFingering: ChordFingering
)

data class ChordFingering(
    val positions: List<ChordPosition>,
    val openStrings: List<Int> = emptyList(),
    val muteStrings: List<Int> = emptyList()
)

data class ChordPosition(
    val finger: Int,
    val fret: Int,
    val strings: List<Int>
)
