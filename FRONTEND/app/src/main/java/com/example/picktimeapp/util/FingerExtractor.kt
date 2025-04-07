package com.example.picktimeapp.util
// mediaPipe에게 좌표를 받아서 사용자의 손 모양을 우리가 원하는 데이터 형식으로 바꾸는 파일

import com.example.picktimeapp.data.model.FingerPlacementJson
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.formats.proto.LandmarkProto

object FingerExtractor {

    fun extractFingeringFromLandmarks(
        landmarks: List<LandmarkProto.NormalizedLandmark>, // 내가 받은 21개의 손가락 포인트
        screenWidth: Int,
        screenHeight: Int
    ): List<FingerPlacementJson> {
        val fingerList = mutableListOf<FingerPlacementJson>()

        // 각 손가락의 TIP 포인트 인덱스 (검지, 중지, 약지, 새끼)
        val fingerTips = mapOf(
            8 to 1,  // index finger → 1
            12 to 2, // middle finger → 2
            16 to 3, // ring finger → 3
            20 to 4  // pinky finger → 4
        )

        // 받은 좌표들을 코드 정보로 변환
        for ((tipIndex, fingerNum) in fingerTips) {
            val tip = landmarks[tipIndex]

            val x = tip.x
            val y = tip.y

            val fret = calculateFret(y)
            val strings = listOf(calculateString(x))

            fingerList.add(
                FingerPlacementJson(
                    finger = fingerNum,
                    fret = fret,
                    strings = strings
                )
            )
        }

        return fingerList
    }

    private fun calculateFret(y: Float): Int {
        return when {
            y < 0.2f -> 5
            y < 0.35f -> 4
            y < 0.5f -> 3
            y < 0.65f -> 2
            else -> 1
        }
    }

    private fun calculateString(x: Float): Int {
        return when {
            x < 0.15f -> 6
            x < 0.3f -> 5
            x < 0.45f -> 4
            x < 0.6f -> 3
            x < 0.75f -> 2
            else -> 1
        }
    }
}