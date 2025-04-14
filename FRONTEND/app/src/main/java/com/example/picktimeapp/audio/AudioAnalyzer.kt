package com.example.picktimeapp.audio

import kotlin.math.log2
import kotlin.math.cos
import org.jtransforms.fft.FloatFFT_1D

object AudioAnalyzer {
    private const val SAMPLE_RATE = 44100

    /**
     * 주어진 오디오 데이터(PCM 16비트 모노)를 FFT하여 Peak 주파수를 반환합니다.
     * @param audioData 오디오 샘플(short[])
     * @return 가장 큰 피크 주파수 (Hz)
     */
    fun analyzeFrequency(audioData: ShortArray): Double {
        val n = audioData.size  // FFT 크기
        // 복소수 배열: 실수와 허수 값을 위해 2배 크기 할당
        val floatData = FloatArray(n * 2)
        for (i in 0 until n) {
            floatData[2 * i] = audioData[i].toFloat() // 실수부
            floatData[2 * i + 1] = 0f                   // 허수부
        }

        // 선택사항: Hann 윈도우 적용 (스펙트럼 Leakage 감소)
        applyHannWindow(floatData)

        // FFT 수행: 생성자에 n을 Long으로 전달
        val fft = FloatFFT_1D(n.toLong())
        fft.complexForward(floatData)  // in-place 변환

        // 스펙트럼에서 가장 큰 피크 찾기 (Nyquist 주파수까지만 의미 있음)
        var maxMagnitude = 0.0
        var maxIndex = 0
        for (i in 0 until n / 2) {
            val real = floatData[2 * i]
            val imag = floatData[2 * i + 1]
            val magnitude = real * real + imag * imag  // 진폭의 제곱
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude.toDouble()
                maxIndex = i
            }
        }

        // 인덱스를 주파수로 변환: bin 간격 = SAMPLE_RATE / n
        val peakFrequency = maxIndex * (SAMPLE_RATE.toDouble() / n)
        return peakFrequency
    }

    /**
     * Hann window를 적용합니다.
     * floatData는 [실수, 허수, 실수, 허수, ...] 형태입니다.
     */
    private fun applyHannWindow(floatData: FloatArray) {
        val n = floatData.size / 2
        for (i in 0 until n) {
            // cos()는 Double 반환 → toFloat()로 변환
            val w = 0.5f * (1f - cos((2.0 * Math.PI * i / (n - 1))).toFloat())
            floatData[2 * i] *= w
        }
    }

    /**
     * 주파수를 음 이름으로 변환합니다. (예: 440Hz -> A4)
     */
    fun frequencyToNoteName(freq: Double): String {
        if (freq <= 0) return "Unknown"

        val A4 = 440.0
        val noteNames = listOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
        val semitones = 12 * log2(freq / A4)
        val nearestNote = (semitones + 0.5).toInt()  // 반올림

        // 보정된 모듈로 연산: 음수일 경우에도 올바른 양의 인덱스를 반환
        val noteIndex = ((nearestNote + 9) % 12 + 12) % 12

        val noteName = noteNames[noteIndex]
        val octave = 4 + ((nearestNote + 9) / 12)
        return "$noteName$octave"
    }
}

