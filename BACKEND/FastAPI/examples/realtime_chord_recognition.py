import time
import sys
import os
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation

# 부모 디렉토리 추가 (src 모듈 임포트를 위해)
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from src.audio.audio_recorder import AudioRecorder
from src.audio.audio_pipeline import GuitarAudioPipeline


class RealTimeChordDisplay:
    """실시간 코드 인식 결과 표시"""

    def __init__(self):
        # 파이프라인 초기화
        self.pipeline = GuitarAudioPipeline()

        # CREPE 모델이 없으면 다운로드
        if not os.path.exists(self.pipeline.pitch_detector.model_path):
            self.pipeline.download_crepe_model(model_size="tiny")

        # 결과 저장용 변수
        self.results = []
        self.timestamps = []
        self.similarities = []
        self.detected_chords = []
        self.current_chord = "Unknown"

        # 플롯 초기화
        self.fig, (self.ax1, self.ax2) = plt.subplots(2, 1, figsize=(10, 8))
        self.fig.suptitle("실시간 기타 코드 인식", fontsize=16)

        # 유사도 그래프
        self.line, = self.ax1.plot([], [], 'b-')
        self.ax1.set_xlim(0, 10)
        self.ax1.set_ylim(0, 1)
        self.ax1.set_xlabel("시간 (초)")
        self.ax1.set_ylabel("유사도")
        self.ax1.set_title("코드 인식 유사도")
        self.ax1.grid(True)

        # 코드 텍스트 표시
        self.chord_text = self.ax2.text(0.5, 0.5, "대기 중...",
                                        fontsize=40, ha='center', va='center')
        self.ax2.set_xlim(0, 1)
        self.ax2.set_ylim(0, 1)
        self.ax2.axis('off')

        # 최근 감지된 코드 목록
        self.recent_text = self.ax2.text(0.5, 0.2, "",
                                         fontsize=12, ha='center', va='center')

        # 오디오 레코더 초기화
        self.recorder = AudioRecorder(callback=self.audio_callback)

    def audio_callback(self, audio_data, sr):
        """오디오 데이터 분석 콜백"""
        try:
            # 오디오 분석
            result = self.pipeline.analyze_audio(audio_data, sr)

            # 결과가 충분히 신뢰할 수 있는 경우만 저장
            if result["chord_similarity"] > 0.5:
                self.results.append(result)
                self.timestamps.append(time.time())
                self.similarities.append(result["chord_similarity"])
                self.detected_chords.append(result["detected_chord"])

                # 최근 3초 데이터만 유지
                current_time = time.time()
                cutoff_time = current_time - 3

                keep_indices = [i for i, t in enumerate(self.timestamps) if t >= cutoff_time]
                self.timestamps = [self.timestamps[i] for i in keep_indices]
                self.similarities = [self.similarities[i] for i in keep_indices]
                self.detected_chords = [self.detected_chords[i] for i in keep_indices]
                self.results = [self.results[i] for i in keep_indices]

                # 가장 최근 분석 결과
                if self.detected_chords:
                    # 최근 감지된 코드 중 가장 빈번한 것 선택
                    from collections import Counter
                    counts = Counter(
                        self.detected_chords[-5:] if len(self.detected_chords) >= 5 else self.detected_chords)
                    self.current_chord = counts.most_common(1)[0][0]

        except Exception as e:
            print(f"분석 오류: {e}")

    def update_plot(self, frame):
        """플롯 업데이트 함수"""
        if self.timestamps:
            # 상대 시간으로 변환
            t0 = self.timestamps[0]
            relative_times = [t - t0 for t in self.timestamps]

            # 플롯 업데이트
            self.line.set_data(relative_times, self.similarities)
            self.ax1.set_xlim(max(0, relative_times[-1] - 10), relative_times[-1] + 0.5)

            # 코드 텍스트 업데이트
            self.chord_text.set_text(self.current_chord)

            # 최근 감지된 코드들 표시
            if len(self.detected_chords) >= 3:
                recent = self.detected_chords[-3:]
                recent_text = f"최근 감지: {', '.join(recent)}"
                self.recent_text.set_text(recent_text)

        return self.line, self.chord_text, self.recent_text

    def run(self):
        """실시간 인식 실행"""
        # 녹음 시작
        self.recorder.start_recording()

        # 애니메이션 설정
        ani = FuncAnimation(self.fig, self.update_plot, interval=100, blit=True)

        # 플롯 표시
        plt.tight_layout()
        plt.show()

        # 녹음 중지
        self.recorder.stop_recording()

        # 결과 저장
        if self.results:
            self.recorder.save_wav("recorded_session.wav")

            # 가장 많이 감지된 코드 찾기
            from collections import Counter
            detected_chords = [r["detected_chord"] for r in self.results]
            most_common = Counter(detected_chords).most_common(3)

            print(f"\n분석 결과:")
            print(f"가장 많이 감지된 코드: {most_common}")
            print(f"총 분석 프레임: {len(self.results)}")


if __name__ == "__main__":
    print("실시간 기타 코드 인식 시작")
    print("종료하려면 플롯 창을 닫으세요.")

    display = RealTimeChordDisplay()
    display.run()