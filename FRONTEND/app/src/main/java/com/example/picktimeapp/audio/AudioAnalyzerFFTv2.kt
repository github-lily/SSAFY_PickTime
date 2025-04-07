import android.util.Log
import org.jtransforms.fft.DoubleFFT_1D
import java.util.Arrays
import kotlin.math.*

object AudioAnalyzerFFTv2 {

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

    // C 코드 후보군 테이블
    val cChordCandidates: List<ChordInfo> = listOf(
        ChordInfo("C",    listOf(0, 4, 7),     listOf("C", "E", "G")),
        ChordInfo("C7",   listOf(0, 4, 7, 10), listOf("C", "E", "G", "Bb")),
        //ChordInfo("C#m",  listOf(0, 3, 7),     listOf("C#", "E", "G#")),
        ChordInfo("Cadd9",listOf(0, 4, 7, 14), listOf("C", "E", "G", "D")),
        //ChordInfo("C#m7", listOf(0, 3, 7, 10), listOf("C#", "E", "G#", "B"))
    )

    // 상수 정의
    const val SAMPLE_RATE = 44100            // 예: 44100 Hz
    const val ATTACK_DELAY_MS = 50           // 스트로크 후 50ms 이후 구간
    const val FRAME_DURATION_MS = 50        // 100ms 길이의 프레임
    const val NUM_FRAMES = 3
    const val OVERLAP_FACTOR = 0.5

    /**
     * ShortArray 타입의 오디오 데이터를 받아 복소수 FFT를 적용합니다.
     *
     * @param audioData 오디오 샘플 데이터 (ShortArray)
     * @return FFT 결과가 담긴 DoubleArray (실제/허수 파트가 interleaved 형식: [Re0, Im0, Re1, Im1, ...])
     */
    fun doFFT(audioData: ShortArray): DoubleArray {
        val n = audioData.size
        // FFT 연산을 위해 2*n 크기의 double 배열 생성 (실수와 허수부)
        val fftData = DoubleArray(2 * n)
        for (i in audioData.indices) {
            fftData[2 * i] = audioData[i].toDouble()   // real part
            fftData[2 * i + 1] = 0.0                       // imaginary part
        }
        val fft = DoubleFFT_1D(n.toLong())
        fft.complexForward(fftData)
        return fftData
    }

    /**
     * 주어진 프레임 데이터에 Hann 윈도우를 적용한 후 FFT를 수행합니다.
     *
     * @param frameData ShortArray 타입의 프레임 오디오 데이터
     * @return 해당 프레임에 대한 FFT 결과 (interleaved 형식의 DoubleArray)
     */
    fun processFrame(frameData: ShortArray): DoubleArray {
        val frameSize = frameData.size
        val windowedShortData = ShortArray(frameSize)
        for (j in 0 until frameSize) {
            val window = 0.5 * (1 - cos(2 * PI * j / (frameSize - 1)))
            val windowedSample = frameData[j] * window
            windowedShortData[j] = windowedSample.roundToInt().toShort()
        }
        return doFFT(windowedShortData)
    }

    /**
     * 오디오 데이터에서 attack 구간 이후에 오버랩을 적용한 다수의 프레임을 추출하여
     * 각각 processFrame 함수를 통해 FFT를 수행하고 결과를 List로 반환합니다.
     *
     * @param audioData ShortArray 타입의 오디오 샘플 데이터
     * @return 각 프레임에 대해 FFT 결과가 담긴 DoubleArray의 List
     */
    fun processMultipleFramesAfterAttack(audioData: ShortArray): List<DoubleArray> {
        //Log.d("processMultipleFramesAfterAttack", "audioData : ${audioData.size}")
        //Log.d("processMultipleFramesAfterAttack", "audioData : ${Arrays.toString(audioData)}")
        val frameSize = (SAMPLE_RATE * (FRAME_DURATION_MS / 1000.0)).toInt()
        val shift = (frameSize * (1 - OVERLAP_FACTOR)).toInt()
        val initialStart = (SAMPLE_RATE * (ATTACK_DELAY_MS / 1000.0)).toInt()

        val fftResults = mutableListOf<DoubleArray>()
        for (i in 0 until NUM_FRAMES) {
            val currentStart = initialStart + i * shift
            if (currentStart + frameSize > audioData.size) break

            val frameData = audioData.copyOfRange(currentStart, currentStart + frameSize)
            fftResults.add(processFrame(frameData))
        }

        return fftResults
    }

    /**
     * 주어진 FFT 결과에 대해 각 후보 코드의 매칭 스코어를 계산하고,
     * 최고 점수를 가진 코드의 이름을 반환합니다.
     *
     * 단, 현재는 루트 음이 C (261.63Hz)라고 가정합니다.
     *
     * @param fftResult 한 프레임에 대한 FFT 결과 (interleaved DoubleArray)
     * @return 해당 프레임에서 검출된 코드 후보의 이름
     */
    fun detectChordForFrame(fftResult: DoubleArray): String {
        // 프레임 크기 계산: doFFT에서 사용한 오디오 데이터 길이
        val frameSize = (SAMPLE_RATE * (FRAME_DURATION_MS / 1000.0)).toInt()
        // FFT의 주파수 해상도 (Hz)
        val binResolution = SAMPLE_RATE.toDouble() / frameSize
        val rootFrequency = 261.63 // C의 주파수 (중음역대 C)

        var bestChord = ""
        var bestScore = 0.0

        for (chord in cChordCandidates) {
            var score = 0.0
            for (interval in chord.intervals) {
                // 예상 주파수 계산: f = rootFrequency * 2^(interval/12)
                val expectedFrequency = rootFrequency * 2.0.pow(interval / 12.0)
                val binIndex = (expectedFrequency / binResolution).roundToInt()
                // FFT 결과 배열의 인덱스 체크 (실제와 허수 파트 고려)
                if (binIndex < 0 || binIndex >= frameSize) continue
                val re = fftResult[2 * binIndex]
                val im = fftResult[2 * binIndex + 1]
                val magnitude = sqrt(re * re + im * im)
                score += magnitude
            }
            if (score > bestScore) {
                bestScore = score
                bestChord = chord.chordName
            }
        }
        return bestChord
    }

    fun detectChordForFrameSpectrum(fftResult: DoubleArray): String {
        // 프레임 크기: doFFT에 사용한 오디오 데이터 길이
        val frameSize = (SAMPLE_RATE * (FRAME_DURATION_MS / 1000.0)).toInt()
        // FFT의 주파수 해상도 (Hz)
        val binResolution = SAMPLE_RATE.toDouble() / frameSize
        val rootFrequency = 261.63 // C의 주파수 (중음역대 C)

        // 1. FFT 결과로부터 각 bin의 진폭을 계산합니다.
        val magnitudes = DoubleArray(frameSize) { i ->
            val re = fftResult[2 * i]
            val im = fftResult[2 * i + 1]
            sqrt(re * re + im * im)
        }
        Log.d("detectChord", "Average magnitude: ${magnitudes.average()}, Max: ${magnitudes.maxOrNull()}, Min: ${magnitudes.minOrNull()}")

        // 2. 간단한 로컬 피크 검출 (양 옆과 비교하여 국소 최대값인 bin만 선택)
        val peakIndices = mutableListOf<Int>()
        // 임계치: 전체 평균의 1.5배 정도를 기준으로 잡습니다. (환경에 따라 조정 가능)
        val threshold = magnitudes.average() * 1.5
        for (i in 1 until frameSize - 1) {
            if (magnitudes[i] > magnitudes[i - 1] &&
                magnitudes[i] >= magnitudes[i + 1] &&
                magnitudes[i] > threshold) {
                peakIndices.add(i)
            }
        }
        Log.d("detectChord", "Detected peak indices: $peakIndices")

        // 3. 각 후보 코드에 대해, 예상 주파수(및 그 주변)의 피크 진폭을 합산하여 매칭 스코어를 계산합니다.
        var bestChord = ""
        var bestScore = 0.0
        for (chord in cChordCandidates) {
            var score = 0.0
            Log.d("detectChord", "Processing chord: ${chord.chordName}")
            for (interval in chord.intervals) {
                // 예상 주파수 계산: f = rootFrequency * 2^(interval/12)
                val expectedFrequency = rootFrequency * 2.0.pow(interval / 12.0)
                val expectedBin = (expectedFrequency / binResolution).roundToInt()
                // 예상 bin 주변(예: ±1 bin)에서 피크 값 검색
                val neighborhood = 1
                var localPeak = 0.0
                for (offset in -neighborhood..neighborhood) {
                    val index = expectedBin + offset
                    if (index in 0 until frameSize && peakIndices.contains(index)) {
                        localPeak = max(localPeak, magnitudes[index])
                    }
                }
                Log.d("detectChord", "Interval: $interval, expectedFreq: $expectedFrequency, expectedBin: $expectedBin, localPeak: $localPeak")
                score += localPeak
            }
            Log.d("detectChord", "Chord: ${chord.chordName}, total score: $score")
            if (score > bestScore) {
                bestScore = score
                bestChord = chord.chordName
            }
        }
        return bestChord
    }

    /**
     * 주어진 FFT 결과에 대해 각 후보 코드의 매칭 스코어를 계산하고,
     * 최고 점수를 가진 코드의 이름을 반환합니다.
     *
     * 단, 현재는 루트 음이 C (261.63Hz)라고 가정합니다.
     *
     * 하모닉 서밍을 적용하여, 각 구성음에 대해 기본 음과 여러 배음의 에너지를 합산합니다.
     *
     * 추가 구성음은 기본 구성음과 별도로 평가한 후,
     * 기본 구성음 대비 에너지 비율이 임계치(additionalThreshold) 이상일 때만 최종 점수에 반영합니다.
     *
     * @param fftResult 한 프레임에 대한 FFT 결과 (interleaved DoubleArray)
     * @return 해당 프레임에서 검출된 코드 후보의 이름
     */
    fun detectChordForFrameHarmonic(fftResult: DoubleArray): String {
        // 프레임 크기 계산
        val frameSize = (SAMPLE_RATE * (FRAME_DURATION_MS / 1000.0)).toInt()
        // FFT 해상도 (Hz)
        val binResolution = SAMPLE_RATE.toDouble() / frameSize
        val rootFrequency = 261.63 // C의 주파수 (중음역대 C)
        Log.d("detectChord", "FrameSize: $frameSize, BinResolution: $binResolution, RootFrequency: $rootFrequency")

        // 하모닉 서밍에서 사용할 최대 배음 개수 (기본 포함)
        val maxHarmonics = 4
        // 신뢰도 검증 임계치: 추가 구성음 에너지 / 기본 구성음 에너지의 비율이 이 값 이상이어야 추가 에너지를 반영
        val additionalThreshold = 0.2
        // 기본 구성음 셋 (다단계 검출의 1단계: 기본 코드 판단)
        val fundamentalIntervals = setOf(0, 4, 7)

        var bestChord = ""
        var bestFinalScore = 0.0

        // 각 후보 코드에 대해 평가
        for (chord in cChordCandidates) {
            // 후보 코드의 구성음들을 기본 구성음과 추가 구성음으로 분리
            val fundamentalSet = chord.intervals.filter { it in fundamentalIntervals }
            val additionalSet = chord.intervals.filter { it !in fundamentalIntervals }

            var fundamentalScore = 0.0
            var additionalScore = 0.0

            Log.d("detectChord", "Processing chord: ${chord.chordName}")

            // 1단계: 기본 구성음 에너지 계산
            for (interval in fundamentalSet) {
                val expectedFrequency = rootFrequency * 2.0.pow(interval / 12.0)
                Log.d("detectChord", "  Fundamental interval: $interval, ExpectedFrequency: $expectedFrequency")
                var energy = 0.0
                for (h in 1..maxHarmonics) {
                    val harmonicFreq = expectedFrequency * h
                    val harmonicBin = (harmonicFreq / binResolution).roundToInt()
                    if (harmonicBin < 0 || harmonicBin >= frameSize) {
                        Log.d("detectChord", "    Harmonic $h: harmonicFreq: $harmonicFreq, harmonicBin: $harmonicBin (out of range)")
                        continue
                    }
                    val re = fftResult[2 * harmonicBin]
                    val im = fftResult[2 * harmonicBin + 1]
                    val magnitude = sqrt(re * re + im * im)
                    val weight = 1.0 / h
                    val weightedMagnitude = weight * magnitude
                    Log.d("detectChord", "    Harmonic $h: harmonicFreq: $harmonicFreq, harmonicBin: $harmonicBin, magnitude: $magnitude, weight: $weight, weightedMagnitude: $weightedMagnitude")
                    energy += weightedMagnitude
                }
                Log.d("detectChord", "  Total fundamental energy for interval $interval: $energy")
                fundamentalScore += energy
            }

            // 2단계: 추가 구성음 에너지 계산
            for (interval in additionalSet) {
                val expectedFrequency = rootFrequency * 2.0.pow(interval / 12.0)
                Log.d("detectChord", "  Additional interval: $interval, ExpectedFrequency: $expectedFrequency")
                var energy = 0.0
                for (h in 1..maxHarmonics) {
                    val harmonicFreq = expectedFrequency * h
                    val harmonicBin = (harmonicFreq / binResolution).roundToInt()
                    if (harmonicBin < 0 || harmonicBin >= frameSize) {
                        Log.d("detectChord", "    Harmonic $h: harmonicFreq: $harmonicFreq, harmonicBin: $harmonicBin (out of range)")
                        continue
                    }
                    val re = fftResult[2 * harmonicBin]
                    val im = fftResult[2 * harmonicBin + 1]
                    val magnitude = sqrt(re * re + im * im)
                    val weight = 1.0 / h
                    val weightedMagnitude = weight * magnitude
                    Log.d("detectChord", "    Harmonic $h: harmonicFreq: $harmonicFreq, harmonicBin: $harmonicBin, magnitude: $magnitude, weight: $weight, weightedMagnitude: $weightedMagnitude")
                    energy += weightedMagnitude
                }
                Log.d("detectChord", "  Total additional energy for interval $interval: $energy")
                additionalScore += energy
            }

            Log.d("detectChord", "Fundamental score for ${chord.chordName}: $fundamentalScore")
            Log.d("detectChord", "Additional score for ${chord.chordName}: $additionalScore")

            // 신뢰도 검증: 추가 구성음의 에너지가 기본 구성음 대비 충분한 경우에만 반영
            val effectiveAdditionalScore = if (additionalSet.isNotEmpty() && (additionalScore / (fundamentalScore + 1e-6) >= additionalThreshold)) {
                additionalScore
            } else {
                0.0
            }

            // 다단계 결정: 기본 구성음 점수와 신뢰할 경우 반영한 추가 구성음 점수를 합산
            val finalScore = fundamentalScore + effectiveAdditionalScore
            Log.d("detectChord", "Final score for ${chord.chordName}: $finalScore (effectiveAdditionalScore: $effectiveAdditionalScore)")

            if (finalScore > bestFinalScore) {
                bestFinalScore = finalScore
                bestChord = chord.chordName
            }
        }

        Log.d("detectChord", "Detected chord for frame: $bestChord with final score: $bestFinalScore")
        return bestChord
    }

    /**
     * 여러 프레임의 FFT 결과를 바탕으로 다수결 방식으로 최종 코드 결과를 산출합니다.
     *
     * @param fftResults 각 프레임에 대해 FFT 결과가 담긴 DoubleArray 리스트
     * @return 다수결 방식으로 선출된 최종 코드 이름
     */
    fun aggregateChordResults(fftResults: List<DoubleArray>): String {
        val voteCount = mutableMapOf<String, Int>()
        fftResults.forEachIndexed { index, fft ->
            val detectedChord = detectChordForFrameHarmonic(fft)
            Log.d("detectedChord", "Frame ${index + 1} detectedChord : $detectedChord")
            voteCount[detectedChord] = voteCount.getOrDefault(detectedChord, 0) + 1
        }
        // 다수결 방식으로 최대 투표수의 코드를 선택 (투표가 없으면 "Unknown" 반환)
        Log.d("finalDetectedChord", "${voteCount.maxByOrNull { it.value }?.key ?: "Unknown"}")
        return voteCount.maxByOrNull { it.value }?.key ?: "Unknown"
    }
}
