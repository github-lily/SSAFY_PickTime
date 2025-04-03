package com.example.picktimeapp.audio

import kotlin.math.*

// 오디오 분석과 코드 검출을 위한 싱글톤 객체
object AudioAnalyzerFFT {

    private const val sampleRate = 44100
    private const val fftSize = 1024
    private var audioBuffer = mutableListOf<Short>()

    data class ChordDetectionResult(
        val chord: String,
        val notes: Set<String>
    )

    // 복소수 클래스 및 연산
    data class Complex(val re: Double, val im: Double) {
        operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
        operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
        operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
        fun magnitude() = sqrt(re * re + im * im)
    }

    // 재귀적인 FFT 구현
    fun fft(input: Array<Complex>): Array<Complex> {
        val n = input.size
        if (n == 1) return arrayOf(input[0])
        if (n % 2 != 0) throw IllegalArgumentException("Input size must be a power of 2")

        val even = Array(n / 2) { input[2 * it] }
        val odd = Array(n / 2) { input[2 * it + 1] }
        val fftEven = fft(even)
        val fftOdd = fft(odd)
        val output = Array(n) { Complex(0.0, 0.0) }
        for (k in 0 until n / 2) {
            val angle = -2.0 * Math.PI * k / n
            val twiddle = Complex(cos(angle), sin(angle))
            output[k] = fftEven[k] + twiddle * fftOdd[k]
            output[k + n / 2] = fftEven[k] - twiddle * fftOdd[k]
        }
        return output
    }

    // 이동평균 방식으로 magnitude 스펙트럼을 스무딩하는 함수
    fun smoothMagnitudes(magnitudes: DoubleArray, windowSize: Int = 5): DoubleArray {
        val smoothed = DoubleArray(magnitudes.size)
        val halfWindow = windowSize / 2
        for (i in magnitudes.indices) {
            var sum = 0.0
            var count = 0
            for (j in max(0, i - halfWindow) until min(magnitudes.size, i + halfWindow + 1)) {
                sum += magnitudes[j]
                count++
            }
            smoothed[i] = sum / count
        }
        return smoothed
    }

    // FFT 결과의 magnitude 스펙트럼에서 피크 인덱스를 검출하는 함수
    fun detectPeaks(magnitudes: DoubleArray, threshold: Double = 10.0): List<Int> {
        val peaks = mutableListOf<Int>()
        for (i in 1 until magnitudes.size - 1) {
            if (magnitudes[i] > magnitudes[i - 1] &&
                magnitudes[i] > magnitudes[i + 1] &&
                magnitudes[i] > threshold
            ) {
                peaks.add(i)
            }
        }
        return peaks
    }

    // 주파수를 note 이름으로 변환 (MIDI 공식 사용)
    fun freqToNoteName(freq: Double): String {
        if (freq <= 0) return ""
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val midi = (69 + 12 * log2(freq / 440.0)).roundToInt()
        return noteNames[midi % 12]
    }

    // 감지된 note 집합으로부터 간단한 코드 이름 판별
    fun detectChord(notes: Set<String>): String {
        val chordMap = mapOf(
            setOf("C", "E", "G") to "C Major",
            setOf("C", "D#", "G") to "C Minor",
            setOf("D", "F#", "A") to "D Major",
            setOf("E", "G#", "B") to "E Major",
            setOf("F", "A", "C") to "F Major",
            setOf("G", "B", "D") to "G Major",
            setOf("A", "C#", "E") to "A Major",
            setOf("B", "D#", "F#") to "B Major"
        )
        for ((chordNotes, chordName) in chordMap) {
            if (notes.containsAll(chordNotes)) {
                return chordName
            }
        }
        return "Unknown Chord"
    }

    // RMS 기반으로 입력 신호의 에너지를 계산하여 무음 여부를 판단하는 함수
    fun isSilent(bufferChunk: ShortArray, silenceThreshold: Double = 500.0): Boolean {
        val sumSquares = bufferChunk.map { it * it.toDouble() }.sum()
        val rms = sqrt(sumSquares / bufferChunk.size)
        return rms < silenceThreshold
    }

    /**
     * 외부에서 호출하여 오디오 데이터를 처리합니다.
     *
     * audioData: AudioCapture에서 전달받은 ShortArray 데이터
     * 리턴: 처리된 각 1024 샘플 단위의 ChordDetectionResult 목록
     */
    fun processAudioData(audioData: ShortArray): List<ChordDetectionResult> {
        val results = mutableListOf<ChordDetectionResult>()
        // 새로운 오디오 데이터를 내부 버퍼에 추가
        audioBuffer.addAll(audioData.toList())

        // 버퍼에 fftSize 이상의 샘플이 모였으면 처리
        while (audioBuffer.size >= fftSize) {
            val bufferChunk = audioBuffer.take(fftSize).toShortArray()
            audioBuffer = audioBuffer.drop(fftSize).toMutableList()

            // 노이즈 게이트: 신호 에너지가 낮으면 처리 건너뛰기
            if (isSilent(bufferChunk)) {
                continue
            }

            // Hamming 창 적용
            val windowedSamples = DoubleArray(fftSize) { i ->
                val hamming = 0.54 - 0.46 * cos(2.0 * Math.PI * i / (fftSize - 1))
                bufferChunk[i].toDouble() * hamming
            }

            // FFT 입력을 위한 복소수 배열 구성
            val complexInput = Array(fftSize) { i -> Complex(windowedSamples[i], 0.0) }
            val fftResult = fft(complexInput)

            // FFT 결과의 절반만 사용하여 magnitude 스펙트럼 계산
            val magnitudes = DoubleArray(fftSize / 2)
            for (i in 0 until fftSize / 2) {
                magnitudes[i] = fftResult[i].magnitude()
            }

            // 스무딩 적용 (이동평균)
            val smoothedMagnitudes = smoothMagnitudes(magnitudes, windowSize = 5)

            // 피크 검출 (threshold 값은 환경에 따라 조정)
            val peakIndices = detectPeaks(smoothedMagnitudes, threshold = 1000.0)

            // 피크 인덱스를 주파수로 변환 후 note 이름 매핑
            val notes = peakIndices.map { index ->
                val frequency = index.toDouble() * sampleRate / fftSize
                freqToNoteName(frequency)
            }.toSet()

            // 코드 매칭
            val chord = detectChord(notes)
            results.add(ChordDetectionResult(chord, notes))
        }
        return results
    }
}