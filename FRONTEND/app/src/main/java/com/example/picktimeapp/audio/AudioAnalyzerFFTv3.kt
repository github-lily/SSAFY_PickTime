import android.util.Log
import org.jtransforms.fft.DoubleFFT_1D
import java.util.Arrays
import kotlin.math.*

object AudioAnalyzerFFTv3 {

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
        ChordInfo("Cadd9",listOf(0, 4, 7, 14), listOf("C", "E", "G", "D")),
    )

    // 상수 정의
    const val SAMPLE_RATE = 44100            // 예: 44100 Hz
    const val ATTACK_DELAY_MS = 50           // 스트로크 후 50ms 이후 구간
    const val FRAME_DURATION_MS = 100        // 100ms 길이의 프레임
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
     * 주어진 프레임 데이터에 Hann 윈도우를 적용한 후, 제로패딩을 통해 FFT 입력 길이를 확장하고 FFT를 수행합니다.
     *
     * @param frameData ShortArray 타입의 프레임 오디오 데이터
     * @return FFT 결과가 담긴 DoubleArray (실제/허수 파트가 interleaved 형식: [Re0, Im0, Re1, Im1, ...])
     */
    fun processFrame(frameData: ShortArray): DoubleArray {
        val frameSize = frameData.size
        // 윈도잉: windowedData를 DoubleArray로 계산
        val windowedData = DoubleArray(frameSize)
        for (j in 0 until frameSize) {
            val window = 0.5 * (1 - cos(2 * PI * j / (frameSize - 1)))
            windowedData[j] = frameData[j] * window
        }
        // 제로패딩: 프레임 길이를 다음 2의 거듭제곱 크기로 확장 (예, 2205 -> 4096)
        val fftLength = nextPowerOfTwo(frameSize)
        val paddedData = DoubleArray(fftLength)
        for (i in 0 until frameSize) {
            paddedData[i] = windowedData[i]
        }
        // 남은 부분은 기본값 0.0

        // 복소수 FFT 입력 배열 생성: 길이 2*fftLength, [Re0, Im0, Re1, Im1, ...]
        val fftInput = DoubleArray(2 * fftLength)
        for (i in 0 until fftLength) {
            fftInput[2 * i] = paddedData[i]    // 실수부
            fftInput[2 * i + 1] = 0.0            // 허수부
        }
        val fft = DoubleFFT_1D(fftLength.toLong())
        fft.complexForward(fftInput)
        return fftInput
    }

    /**
     * 오디오 데이터에서 attack 구간 이후에 오버랩을 적용한 다수의 프레임을 추출하여,
     * 각 프레임에 대해 processFrame 함수를 호출해 FFT 결과 리스트를 반환합니다.
     *
     * @param audioData ShortArray 타입의 오디오 샘플 데이터
     * @return 각 프레임에 대한 FFT 결과 리스트
     */
    fun processMultipleFramesAfterAttack(audioData: ShortArray): List<DoubleArray> {
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
     * 주어진 FFT 결과를 바탕으로, 각 코드 후보에 대해 하모닉 에너지를 계산하여 매칭 스코어를 산출하고,
     * 최고 점수를 가진 코드 후보의 이름을 반환합니다.
     *
     * 단, 현재는 루트 음이 C (261.63Hz)라고 가정합니다.
     *
     * @param fftResult 한 프레임에 대한 FFT 결과 (interleaved DoubleArray)
     * @return 해당 프레임에서 검출된 코드 후보의 이름
     */
    fun detectChordForFrameHarmonic(fftResult: DoubleArray): String {
        // 원래 프레임 길이 및 제로패딩된 FFT 길이 계산
        val originalFrameSize = (SAMPLE_RATE * (FRAME_DURATION_MS / 1000.0)).toInt()
        val fftLength = nextPowerOfTwo(originalFrameSize)
        // FFT 해상도는 제로패딩된 길이를 기준으로 함
        val binResolution = SAMPLE_RATE.toDouble() / fftLength
        val rootFrequency = 261.63
        Log.d("detectChord", "FFT Length: $fftLength, BinResolution: $binResolution, RootFrequency: $rootFrequency")

        val maxHarmonics = 4
        val additionalThreshold = 0.1
        val fundamentalIntervals = setOf(0, 4, 7)

        var bestChord = ""
        var bestFinalScore = 0.0

        for (chord in cChordCandidates) {
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
                    if (harmonicBin < 0 || harmonicBin >= fftLength) {
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
                    if (harmonicBin < 0 || harmonicBin >= fftLength) {
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

            // 신뢰도 검증: 추가 구성음 에너지가 충분한 경우에만 반영
            val effectiveAdditionalScore = if (additionalSet.isNotEmpty() && (additionalScore / (fundamentalScore + 1e-6) >= additionalThreshold)) {
                additionalScore
            } else {
                0.0
            }

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

    // 주어진 정수의 다음 2의 거듭제곱을 구하는 헬퍼 함수
    private fun nextPowerOfTwo(n: Int): Int {
        var power = 1
        while (power < n) {
            power *= 2
        }
        return power
    }

}
