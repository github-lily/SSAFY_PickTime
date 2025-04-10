package com.example.picktimeapp.controller

import com.example.picktimeapp.audio.AudioAnalyzerYIN
import com.example.picktimeapp.audio.AudioEventListener
import com.example.picktimeapp.audio.AudioComm
import com.example.picktimeapp.util.CameraFrameAnalyzer

/**
 * 오디오 이벤트 발생 시 카메라 캡처를 시작하는 컨트롤러
 */
class AudioCaptureController(
    private val cameraFrameAnalyzer: CameraFrameAnalyzer
) : AudioEventListener {

    init {
        // AudioComm에 이벤트 리스너로 자신을 등록합니다.
        AudioComm.eventListener = this
    }

    override fun onThresholdExceeded(audioData: ShortArray) {
        // 오디오 임계치 초과 이벤트 발생하면 카메라 프레임 캡처를 시작합니다.
        // 예를 들어 UI 스레드로 전환할 필요가 있다면 Handler나 코루틴으로 감싸서 호출하세요.

        val estimatedChord = AudioAnalyzerYIN.detectChordName(audioData)

        cameraFrameAnalyzer.startCapture(estimatedChord)
    }
}