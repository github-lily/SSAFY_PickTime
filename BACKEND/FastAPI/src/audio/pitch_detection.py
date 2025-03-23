import numpy as np
import librosa
import crepe
import time


class CREPEPitchDetector:
    """CREPE 라이브러리 기반 음높이 감지"""

    def __init__(self, model_capacity="tiny"):
        """
        CREPE 모델 초기화

        Args:
            model_capacity: 모델 크기 ("tiny", "small", "medium", "large", "full")
        """
        self.sample_rate = 16000  # CREPE는 16kHz로 고정
        self.model_capacity = model_capacity
        self.hop_length = 160  # 10ms (CREPE 기본값)

        # 모델 초기화 (첫 호출 때 모델이 로드됨)
        self._initialize_model()

    def _initialize_model(self):
        """모델 초기화 - 빠른 첫 추론을 위해 더미 데이터로 워밍업"""
        dummy_audio = np.zeros(self.sample_rate)  # 1초 길이 더미 오디오
        _ = crepe.predict(
            dummy_audio,
            self.sample_rate,
            model_capacity=self.model_capacity,
            step_size=1000,  # 1초마다 한 번씩 예측 (빠른 워밍업용)
            verbose=False
        )

    def preprocess_audio(self, audio, sr):
        """오디오 전처리: 리샘플링 및 정규화"""
        if sr != self.sample_rate:
            audio = librosa.resample(audio, orig_sr=sr, target_sr=self.sample_rate)

        # 정규화 (CREPE 라이브러리에서 내부적으로 처리하지만 일관성을 위해 유지)
        audio = audio.astype(np.float32)
        return audio

    def extract_pitch(self, audio, sr):
        """
        오디오에서 음높이(주파수) 추출

        Args:
            audio: 오디오 신호 (numpy array)
            sr: 샘플링 레이트

        Returns:
            times: 시간 프레임
            frequencies: 감지된 주파수
            confidences: 각 주파수의 신뢰도
        """
        start_time = time.time()

        # 오디오 전처리
        audio = self.preprocess_audio(audio, sr)

        # CREPE로 피치 추출
        time_step = 10  # 10ms 단위로 예측 (기본값)

        # CREPE 예측 실행
        times, frequencies, confidences, activations = crepe.predict(
            audio,
            self.sample_rate,
            model_capacity=self.model_capacity,
            step_size=time_step,
            verbose=False
        )

        # 처리 시간 로깅
        process_time = time.time() - start_time
        print(f"CREPE 피치 추출 시간: {process_time:.3f}초 (길이: {len(audio) / sr:.2f}초)")

        return times, frequencies, confidences