package com.example.picktimeapp.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 오디오 캡쳐 객체.
 * onAudioDataCaptured 콜백을 통해 캡쳐된 오디오 데이터를 ViewModel 등으로 전달할 수 있습니다.
 */
class AudioCapture(
    private val BUFFER_SIZE : Int,
    // 캡쳐된 오디오 데이터(ShortArray)를 전달하는 콜백.
    private val onAudioDataCaptured: (ShortArray) -> Unit
) {
    // 샘플링 주파수, 모노 채널, 16비트 PCM 인코딩 사용
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    // 일단 기본 minBufferSize (1024~2048 정도로 나오는 경우가 많음)
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    // 실제로는 이 정도까지 늘려서 read해도 됨 (4096)
    private val bufferSize = maxOf(minBufferSize, BUFFER_SIZE)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var captureJob: Job? = null

    /**
     * 오디오 녹음을 시작합니다.
     */
    fun startRecording() {
        if (isRecording) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        ).also { record ->
            record.startRecording()
        }

        isRecording = true

        // 백그라운드 코루틴에서 오디오 데이터 읽기 시작
        captureJob = CoroutineScope(Dispatchers.Default).launch {
            // 버퍼 크기만큼 읽어들임
            val buffer = ShortArray(bufferSize)
            while (isRecording && audioRecord != null) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    // 실제 읽은 크기만큼 복사하여 콜백 전달
                    val audioData = buffer.copyOf(readSize)
                    onAudioDataCaptured(audioData)
                }
            }
        }
    }

    /**
     * 오디오 녹음을 중지하고 자원을 해제합니다.
     */
    fun stopRecording() {
        isRecording = false
        captureJob?.cancel()
        //captureJob?.join()
        captureJob = null
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
    }
}