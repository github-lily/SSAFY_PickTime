package com.example.picktimeapp.audio

import android.util.Log
import be.tarsos.dsp.util.fft.FFT
import java.util.Arrays
import kotlin.math.*

object AudioAnalyzerFFT {

    /**
     * 특정 코드 하나에 대한 정보를 담는 클래스
     * - chordName: 코드 이름 (예: "C7")
     * - intervals: 루트(root)를 0으로 했을 때, 다른 음들까지의 반음 간격
     * - chordTones: 코드 구성음 (간단히 문자열로만 표기)
     */
    data class ChordInfo(
        val chordName: String,
        val intervals: List<Int>,
        val chordTones: List<String>
    )

    // C 코드 후보군 테이블 (이전과 동일)
    val cChordCandidates: List<ChordInfo> = listOf(
        ChordInfo("C",    listOf(0, 4, 7),    listOf("C", "E", "G")),
        ChordInfo("C7",   listOf(0, 4, 7, 10),listOf("C", "E", "G", "Bb")),
        ChordInfo("C#m",  listOf(0, 3, 7),    listOf("C#", "E", "G#")),
        ChordInfo("Cadd9",listOf(0, 4, 7, 14),listOf("C", "E", "G", "D")),
        ChordInfo("C#m7", listOf(0, 3, 7, 10),listOf("C#", "E", "G#", "B"))
    )

    // -- 내부에서 사용될 상수들: 샘플레이트, 스킵 ms, 분석 구간 길이, 프레임 크기/오버랩 등 --
    private const val SAMPLE_RATE = 44100          // 고정 샘플레이트 가정
    private const val SKIP_MS = 200               // 어택 구간 스킵
    private const val ANALYSIS_WINDOW_MS = 200    // 분석할 구간 길이
    private const val FRAME_SIZE = 1024           // FFT 프레임 크기
    private const val FRAME_STEP = 512            // 프레임 이동 스텝(50% overlap)

    /**
     * 오디오 데이터(ShortArray)를 입력받아,
     * - 초반 SKIP_MS(200ms) 구간을 건너뛰고,
     * - ANALYSIS_WINDOW_MS(200ms) 길이만큼을 프레임 단위로 잘라
     * - FFT를 수행하여 각 프레임의 파워 스펙트럼을 List로 반환
     */
    fun analyzeAudioDataInFrames(
        audioData: ShortArray
    ): List<FloatArray> {

        // 1) ms -> 샘플(sample) 단위로 계산
        val skipSamples = (SAMPLE_RATE * SKIP_MS) / 1000
        val analysisSamples = (SAMPLE_RATE * ANALYSIS_WINDOW_MS) / 1000

        // 2) skip 구간 + 분석할 구간을 잘라낸다
        val startIndex = 0
        // endIndex = start + analysisSamples (범위를 넘어가지 않도록 min 처리)
        val endIndex = (startIndex + analysisSamples).coerceAtMost(audioData.size)

        if (startIndex >= endIndex) {
            // 만약 스킵 범위가 전체보다 크다면, 빈 결과 반환
            Log.d("skip", "skipskipskipskipskip")
            return emptyList()
        }

        // 3) 분석 구간만큼의 ShortArray 추출
        val analysisData = audioData.copyOfRange(startIndex, endIndex)

        // 4) 프레임 단위로 잘라가며 FFT 수행
        val resultSpectrums = mutableListOf<FloatArray>()

        var frameStart = 0
        while (frameStart + FRAME_SIZE <= analysisData.size) {
            // (a) 현재 프레임 추출
            val frameShortArray = analysisData.copyOfRange(frameStart, frameStart + FRAME_SIZE)

            // (b) 프레임별 FFT → power spectrum
            val powerSpectrum = doFFT(frameShortArray)
            resultSpectrums.add(powerSpectrum)
            Log.d("spectrum", "${frameStart} ${Arrays.toString(powerSpectrum)}")
            // (c) 다음 프레임으로 이동
            frameStart += FRAME_STEP
        }

        return resultSpectrums
    }

    /**
     * 단일 프레임(ShortArray)을 받아서
     * - float 변환 + 해닝 윈도우
     * - TarsosDSP FFT (powerPhaseFFT)
     * - power 스펙트럼 반환
     */
    private fun doFFT(audioData: ShortArray): FloatArray {
        // 1) short -> float 변환
        val floatBuffer = FloatArray(audioData.size) { i ->
            // short -> float 변환
            audioData[i].toFloat()
        }

        // 2) 해닝 윈도우(Hanning Window) 적용
        //    (간단 예시: 0.5 - 0.5*cos(2πn/(N-1)))
        for (i in floatBuffer.indices) {
            val window = (0.5 - 0.5 * cos(2.0 * PI * i / (floatBuffer.size - 1))).toFloat()
            floatBuffer[i] *= window
        }

        // 3) FFT 객체 생성 (버퍼 크기에 맞게)
        val fft = FFT(floatBuffer.size)

        // 4) power, phase 배열 준비
        //    floatBuffer.length는 복소수 변환 이후 2배 길이를 사용하지만,
        //    power/phase는 그 절반 크기만 필요합니다.
        val powerArray = FloatArray(floatBuffer.size / 2)
        val phaseArray = FloatArray(floatBuffer.size / 2)

        // 5) powerPhaseFFT() 호출로
        //    - 내부적으로 realForward() 수행
        //    - power, phase 계산
        fft.powerPhaseFFT(floatBuffer, powerArray, phaseArray)

        return powerArray
    }

    // -----------------------------
    // (3) 주파수 bin → 실제 주파수(Hz) 변환 함수
    // -----------------------------
    private fun binIndexToFrequency(binIndex: Int, fftSize: Int, sampleRate: Int = SAMPLE_RATE): Double {
        // FFT 결과에서 binIndex = (sampleRate / fftSize) * binIndex
        // 0 ~ fftSize/2 범위
        return binIndex.toDouble() * sampleRate / fftSize
    }

    // -----------------------------
    // (4) 주파수(Hz) → 반음(MIDI note) 변환 함수
    // -----------------------------
    private fun frequencyToMidiNote(freq: Double): Double {
        // MIDI 69 = A4 = 440Hz
        // n = 69 + 12 * log2(freq / 440)
        // freq <= 0이면 예외처리
        if (freq <= 0.0) return 0.0
        return 69.0 + 12.0 * log2(freq / 440.0)
    }

    // -----------------------------
    // (5) 피크 추출(간단 버전)
    // -----------------------------
    /**
     * 파워 스펙트럼에서 상위 N개 피크 bin을 찾고,
     * 각 bin을 Hz로 변환한 리스트를 반환
     */
    private fun extractPeaks(powerSpectrum: FloatArray, topN: Int = 5, threshold: Float = 0.0f): List<Double> {
        // 파워와 인덱스를 묶어서 정렬
        val indexedPower = powerSpectrum.mapIndexed { index, value -> index to value }
        // threshold 이상이면서 상위 N개를 택한다
        val topBins = indexedPower
            .filter { it.second >= threshold }
            .sortedByDescending { it.second }
            .take(topN)

        // binIndex -> freq(Hz)
        return topBins.map { (binIndex, _) ->
            binIndexToFrequency(binIndex, powerSpectrum.size * 2) // fftSize = powerSpectrum.size*2
        }
    }

    // -----------------------------
    // (6) 코드 매칭 로직
    // -----------------------------
    /**
     * 여러 프레임의 파워 스펙트럼을 받아,
     * - 각 프레임에서 피크 주파수를 추출
     * - 피크 주파수를 MIDI note(반음)로 변환
     * - 프레임별 혹은 전체 종합해 코드 후보와 매칭, 최종 코드를 결정
     */
    fun detectChordFromSpectrums(
        spectrums: List<FloatArray>,
        chordCandidates: List<ChordInfo> = cChordCandidates
    ): String? {

        // 1) 모든 프레임에 대해 피크(Hz) → MIDI note로 변환
        //    여기서는 간단히 상위 5개 피크만 추출 예시
        val allDetectedNotes = mutableListOf<Int>()

        for (spectrum in spectrums) {
            val peakFreqs = extractPeaks(spectrum, topN = 5, threshold = 0.0f)
            // 주파수 -> MIDI note(반올림)
            val midiNotes = peakFreqs.map { freq ->
                frequencyToMidiNote(freq).roundToInt()
            }
            allDetectedNotes.addAll(midiNotes)
        }

        if (allDetectedNotes.isEmpty()) {
            Log.d("FFT", "no peak detected")
            return null // 피크가 없으면 코드 판정 불가
        }

        // 2) 코드 후보마다 스코어 계산
        var bestChord: String? = null
        var bestScore = -99999

        chordCandidates.forEach { chord ->
            val score = computeChordScore(chord, allDetectedNotes)
            if (score > bestScore) {
                bestScore = score
                bestChord = chord.chordName
            }
        }

        return bestChord
    }

    /**
     * 특정 코드(C, C7 등)와, 검출된 MIDI 노트 리스트를 비교하여
     * 간단히 스코어를 계산
     */
    private fun computeChordScore(chord: ChordInfo, detectedNotes: List<Int>): Int {
        // 예시)
        //  - chord의 구성음 (C, E, G 등)을 MIDI number로 미리 변환해서
        //    detectedNotes에 얼마나 포함되는지 체크
        //  - chord 구성음 중 많이 검출될수록 + 점수
        //  - chord와 충돌되는 음정 감점

        // A) chordTone -> MIDI 변환 (옥타브에 상관없이 12 반음 모듈로만 비교)
        val chordMidiMod12 = chord.chordTones.map {
            val noteName = it.uppercase()
            nameToMidiMod12(noteName)
        }.toSet() // 중복 제거

        // B) detectedNotes의 각 노트(n) -> n mod 12와 chordMidiMod12가 매칭되는지
        var score = 0
        for (note in detectedNotes) {
            val mod12 = (note % 12 + 12) % 12
            if (chordMidiMod12.contains(mod12)) {
                score += 15 // 코드 구성음이면 +15
            }
            else if (mod12 in chordMidiMod12) {
                score += 8  // 확장음(9도,7도 등)은 좀 덜
            }
            else {
                score -= 5  // 코드에 없는 음이면 약간 감점
            }
        }

        return score
    }

    /**
     * 간단한 음이름->mod12 변환 예시 (C=0, C#=1, D=2, ...)
     * 실제론 더 많은 변환(명음/이명, B#/Cb 등) 처리 필요할 수 있음
     */
    private fun nameToMidiMod12(noteName: String): Int {
        return when (noteName) {
            "C" -> 0
            "C#" -> 1
            "DB" -> 1
            "D" -> 2
            "D#" -> 3
            "EB" -> 3
            "E" -> 4
            "F" -> 5
            "F#" -> 6
            "GB" -> 6
            "G" -> 7
            "G#" -> 8
            "AB" -> 8
            "A" -> 9
            "A#" -> 10
            "BB" -> 10
            "B" -> 11
            "CB" -> 11  // 예외적 표기
            else -> -1  // 알 수 없는 경우
        }
    }
}