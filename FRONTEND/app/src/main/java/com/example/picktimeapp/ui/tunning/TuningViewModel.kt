package com.example.picktimeapp.ui.tunning

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.picktimeapp.audio.AudioCapture
import com.example.picktimeapp.audio.AudioPlayer
import javax.inject.Inject

@HiltViewModel
class TuningViewModel @Inject constructor() : ViewModel() {

    // 기린 이미지의 X 오프셋
    val girinOffsetX = Animatable(-50f)

    private val _targetOffsetY = mutableStateOf(900f)
    val targetOffsetY: State<Float> = _targetOffsetY

    // 디버그 텍스트 상태 (오디오 캡쳐 정보를 화면에 표시하기 위해)
    private val _audioDebugInfo = mutableStateOf("아직 오디오 데이터가 없습니다.")
    val audioDebugInfo: State<String> = _audioDebugInfo

    // AudioPlayer 객체: AudioTrack으로 오디오 플레이백
    private val audioPlayer = AudioPlayer()

    // AudioCapture 객체 생성: 콜백으로 오디오 데이터를 받음
    // 여기서 콜백으로 수신된 데이터는 AudioPlayer로 플레이백하고, 디버그 정보를 업데이트합니다.
    private val audioCapture = AudioCapture { audioData ->
        _audioDebugInfo.value = "오디오 캡쳐됨: ${audioData.size} 샘플"
        Log.d("TuningViewModel", "오디오 캡쳐됨: ${audioData.size} 샘플")
        // AudioPlayer를 통해 오디오 데이터를 플레이백합니다.
        audioPlayer.playAudioData(audioData)
    }

    /**
     * 오디오 캡쳐와 플레이백을 시작합니다.
     */
    fun startAudioCaptureAndPlayback() {
        audioPlayer.start() // AudioTrack 시작
        audioCapture.startRecording()
    }

    /**
     * 오디오 캡쳐와 플레이백을 중지합니다.
     */
    fun stopAudioCaptureAndPlayback() {
        audioCapture.stopRecording()
        audioPlayer.stop()
    }
}
