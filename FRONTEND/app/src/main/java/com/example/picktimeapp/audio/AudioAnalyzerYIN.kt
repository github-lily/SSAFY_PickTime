package com.example.picktimeapp.audio

import kotlin.math.log2

/**
 * YIN 알고리즘 기반의 오디오 분석
 *  - 입력: 16비트 PCM 모노 샘플 (ShortArray)
 *  - 출력: 추정된 기본 주파수 (Double, Hz 단위)
 *
 * YIN 알고리즘 개요:
 *  1) 차분(difference) 함수 계산
 *  2) 누적 평균(normalized) 차분 계산
 *  3) 임계값을 이용해 가장 낮은 tau(주기) 탐색
 *  4) (선택) 보간(Interpolation)을 통해 주파수 정밀도 향상
 */
object AudioAnalyzerYIN {
    private const val SAMPLE_RATE = 44100     // 실제 샘플레이트에 맞춰 조정 필요
    private const val YIN_THRESHOLD = 0.15f    // 음원 상황에 따라 조정(0.05~0.2 등)

    /**
     * YIN 알고리즘으로 주파수를 분석해 반환합니다.
     *
     * @param audioData 16비트 PCM 모노 데이터
     * @return 추정된 기본 주파수(Hz). 유효한 피치를 찾지 못하면 0.0 반환
     */
    fun analyzeFrequency(audioData: ShortArray): Double {
        if (audioData.isEmpty()) return 0.0

        // 1) ShortArray -> FloatArray 변환 ([-1.0, 1.0] 범위)
        val floatData = FloatArray(audioData.size)
        for (i in audioData.indices) {
            floatData[i] = audioData[i] / 32768.0f
        }

        // YIN에서 비교를 위해 보통 (n/2) 길이만큼만 차분 계산
        val halfSize = floatData.size / 2
        val difference = FloatArray(halfSize)

        // 2) difference function 계산
        //    difference[tau] = Σ (x[j] - x[j+tau])^2 (j=0 to halfSize-1)
        //    (tau는 1 ~ halfSize-1)
        for (tau in 1 until halfSize) {
            var sum = 0f
            for (j in 0 until halfSize) {
                val delta = floatData[j] - floatData[j + tau]
                sum += delta * delta
            }
            difference[tau] = sum
        }

        // 3) 누적 평균(normalized) 차분 계산
        //    difference[tau] = difference[tau] / ( (1/tau) Σ( difference[k], k=1..tau ) )
        difference[0] = 1f // 0으로 두면 NaN 가능성이 있으므로 임의로 1
        var cumulativeSum = 0f
        for (tau in 1 until halfSize) {
            cumulativeSum += difference[tau]
            // 누적합으로 나누어 정규화
            difference[tau] = difference[tau] * tau / cumulativeSum
        }

        // 4) 임계값(YIN_THRESHOLD) 이하인 첫 번째 지점을 찾되,
//    다음 샘플이 더 작은지 확인하여 로컬 최소값을 찾는다.
        var tauEstimate = -1
        for (i in 2 until halfSize) {
            var t = i
            if (difference[t] < YIN_THRESHOLD) {
                // 만약 바로 다음 t가 더 작다면, 해당 t로 이동
                while (t + 1 < halfSize && difference[t + 1] < difference[t]) {
                    t++
                }
                tauEstimate = t
                break
            }
        }

        // 유효한 피치를 찾지 못했다면 0.0 반환
        if (tauEstimate == -1) {
            return 0.0
        }

        // 5) (선택) 파라볼라 보간(Parabolic Interpolation)을 적용해 주파수 정확도 향상
        //    tau 근처의 difference 값을 이용해 보간.
        //    tau' = tau - 0.5 * (d[tau+1] - d[tau-1]) / (2*d[tau] - d[tau+1] - d[tau-1])
        val x1 = tauEstimate
        val x0 = if (x1 <= 0) x1 else x1 - 1
        val x2 = if (x1 + 1 < difference.size) x1 + 1 else x1
        // 보간 가능한 범위가 아닐 경우(경계값), 단순 계산
        if (x0 == x1 || x2 == x1) {
            // 보간 불가능 → 대략적인 SAMPLE_RATE / tauEstimate 반환
            return SAMPLE_RATE.toDouble() / tauEstimate
        } else {
            val s0 = difference[x0]
            val s1 = difference[x1]
            val s2 = difference[x2]
            // YIN 공식에 따른 보간 계산
            val betterTau = x1 + 0.5f * (s0 - s2) / (s0 - 2f * s1 + s2)
            return SAMPLE_RATE.toDouble() / betterTau
        }
    }

    /**
     * 기존과 동일한 방식으로, Hz → 노트 이름을 변환합니다.
     *  ex) 440 Hz -> A4
     */
    fun frequencyToNoteName(freq: Double): String {
        if (freq <= 0) return "Unknown"

        // MIDI 표준: A4 = 440Hz, MIDI note=69
        val midiA4 = 69
        val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

        // freq에 대한 semitone 차이를 구해서 MIDI 노트 번호로 매핑
        val semitonesFromA4 = 12.0 * log2(freq / 440.0)
        val midiNote = (midiA4 + semitonesFromA4 + 0.5).toInt()  // 반올림

        // 노트 이름(0=C, 1=C#, ..., 11=B)
        val noteIndex = midiNote % 12
        // MIDI에서 옥타브 계산: A4=69 → 69/12=5, 그러나 보통 0옥타브가 MIDI 노트 12 ~ 23 범위이므로 -1
        val octave = (midiNote / 12) - 1

        val noteName = noteNames[noteIndex]
        return "$noteName$octave"
    }

    fun detectChordName(audioData: ShortArray): String {
        // 1. 주파수 분석
        val freq = AudioAnalyzerYIN.analyzeFrequency(audioData)
        if (freq <= 0.0) return "Unknown"

        // 2. 노트 이름으로 변환 (예: "E4")
        val noteName = AudioAnalyzerYIN.frequencyToNoteName(freq)

        // 3. 옥타브 제거해서 루트 노트 추출 (예: "E4" -> "E", "C#3" -> "C#")
        val root = noteName.filter { it.isLetter() || it == '#' }

        // 4. 유효 코드면 반환
        val validRoots = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        return if (root in validRoots) root else "Unknown"
    }

}
