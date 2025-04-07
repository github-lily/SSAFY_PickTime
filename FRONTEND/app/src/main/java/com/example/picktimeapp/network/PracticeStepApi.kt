package com.example.picktimeapp.network

import retrofit2.http.GET

// 전체 응답

data class PracticeStepResponse(
    val stepType : Int,
    val chords: List<ChordResponse>?,
    val song: SongResponse?
)

// 코드 정보

data class ChordResponse(
    val chordName: String,
    val chordImageUri: String,
    val chordSoundUri: String,
    val chordFingering: ChordFingering
)

data class ChordFingering(
    val positions: List<FingeringPosition>,
    val openStrings: List<Int>,
    val muteStrings: List<Int>
)

data class FingeringPosition(
    val finger: Int,
    val fret: Int,
    val strings: List<Int>
)

// 노래 정보

data class SongResponse(
    val title: String,
    val bpm: Int,
    val artist: String,
    val durationSec: Int,
    val timeSignature: String,
    val chordProgression: List<ChordMeasure>,
    val songUri: String,
    val organizedChords: List<String>? = null
)

data class ChordMeasure(
    val measureIndex: Int,
    val chordBlocks: List<String>
)



interface PracticeStepApi {
    @GET("practice/{stepId}")
    suspend fun getPracticeStep(
        @retrofit2.http.Path("stepId") stepId: Int
    ): retrofit2.Response<PracticeStepResponse>
}
