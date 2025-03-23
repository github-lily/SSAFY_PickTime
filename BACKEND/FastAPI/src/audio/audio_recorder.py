import pyaudio
import wave
import numpy as np
import threading
import time
import queue


class AudioRecorder:
    """실시간 오디오 레코딩 및 분석을 위한 클래스"""

    def __init__(self, callback=None, rate=16000, chunk_size=1024, channels=1):
        """
        오디오 레코더 초기화

        Args:
            callback: 오디오 청크를 처리할 콜백 함수
            rate: 샘플링 레이트
            chunk_size: 오디오 청크 크기
            channels: 오디오 채널 수
        """
        self.rate = rate
        self.chunk_size = chunk_size
        self.channels = channels
        self.format = pyaudio.paFloat32
        self.callback = callback

        self.audio = pyaudio.PyAudio()
        self.stream = None
        self.is_recording = False
        self.frames = []

        # 분석용 큐
        self.audio_queue = queue.Queue()
        self.analysis_thread = None

    def _audio_callback(self, in_data, frame_count, time_info, status):
        """PyAudio 콜백 함수"""
        if self.is_recording:
            self.frames.append(in_data)

            # 오디오 데이터를 numpy 배열로 변환
            audio_data = np.frombuffer(in_data, dtype=np.float32)

            # 분석 큐에 넣기
            self.audio_queue.put(audio_data)

        return (in_data, pyaudio.paContinue)

    def _analysis_worker(self):
        """오디오 분석 작업자 스레드"""
        buffer = np.array([], dtype=np.float32)
        min_buffer_size = int(1.0 * self.rate)  # 1초 버퍼

        while self.is_recording:
            try:
                # 새 오디오 데이터 가져오기
                audio_chunk = self.audio_queue.get(timeout=0.1)

                # 버퍼에 추가
                buffer = np.append(buffer, audio_chunk)

                # 버퍼가 충분히 크면 분석 실행
                if len(buffer) >= min_buffer_size:
                    # 콜백 함수 호출
                    if self.callback:
                        self.callback(buffer, self.rate)

                    # 버퍼 슬라이딩 윈도우 업데이트 (50% 오버랩)
                    overlap = int(len(buffer) * 0.5)
                    buffer = buffer[-overlap:]

            except queue.Empty:
                continue
            except Exception as e:
                print(f"분석 오류: {e}")

    def start_recording(self):
        """녹음 시작"""
        if self.stream is not None:
            return

        self.is_recording = True
        self.frames = []

        # 오디오 스트림 열기
        self.stream = self.audio.open(
            format=self.format,
            channels=self.channels,
            rate=self.rate,
            input=True,
            frames_per_buffer=self.chunk_size,
            stream_callback=self._audio_callback
        )

        # 분석 스레드 시작
        if self.callback:
            self.analysis_thread = threading.Thread(target=self._analysis_worker)
            self.analysis_thread.daemon = True
            self.analysis_thread.start()

        print("녹음 시작...")

    def stop_recording(self):
        """녹음 중지"""
        if self.stream is None:
            return

        self.is_recording = False

        # 스트림 닫기
        self.stream.stop_stream()
        self.stream.close()
        self.stream = None

        # 분석 스레드 종료 대기
        if self.analysis_thread:
            self.analysis_thread.join(timeout=1.0)
            self.analysis_thread = None

        print("녹음 중지")

        return self.frames

    def save_wav(self, filename):
        """WAV 파일로 저장"""
        if not self.frames:
            print("저장할 오디오 데이터가 없습니다.")
            return

        wf = wave.open(filename, 'wb')
        wf.setnchannels(self.channels)
        wf.setsampwidth(self.audio.get_sample_size(pyaudio.paInt16))
        wf.setframerate(self.rate)

        # float32를 int16으로 변환
        audio_data = np.frombuffer(b''.join(self.frames), dtype=np.float32)
        audio_data = np.clip(audio_data, -1.0, 1.0)
        audio_data = (audio_data * 32767).astype(np.int16)

        wf.writeframes(audio_data.tobytes())
        wf.close()

        print(f"오디오 파일 저장됨: {filename}")

    def __del__(self):
        """소멸자"""
        if self.stream:
            self.stream.close()

        self.audio.terminate()


# 간단한 사용 예제
if __name__ == "__main__":
    import matplotlib.pyplot as plt
    from audio_pipeline import GuitarAudioPipeline

    # 분석 파이프라인 초기화
    pipeline = GuitarAudioPipeline()

    # 결과 저장용 변수
    results = []


    # 콜백 함수
    def audio_callback(audio_data, sr):
        result = pipeline.analyze_audio(audio_data, sr)
        results.append(result)
        print(f"감지된 코드: {result['detected_chord']} (유사도: {result['chord_similarity']:.2f})")


    # 오디오 레코더 초기화
    recorder = AudioRecorder(callback=audio_callback)

    try:
        # 녹음 시작
        recorder.start_recording()

        # 10초 동안 녹음
        time.sleep(10)

        # 녹음 중지
        recorder.stop_recording()

        # 결과 저장
        recorder.save_wav("recorded_chord.wav")

        # 결과 분석
        if results:
            # 가장 많이 감지된 코드 찾기
            detected_chords = [r["detected_chord"] for r in results]
            most_common = max(set(detected_chords), key=detected_chords.count)

            print(f"\n분석 결과:")
            print(f"가장 많이 감지된 코드: {most_common}")
            print(f"총 분석 프레임: {len(results)}")

            # 신뢰도 시각화
            similarities = [r["chord_similarity"] for r in results]
            plt.figure(figsize=(10, 5))
            plt.plot(similarities)
            plt.title(f"코드 인식 유사도 변화 ({most_common})")
            plt.xlabel("프레임")
            plt.ylabel("유사도")
            plt.ylim(0, 1)
            plt.savefig("chord_similarity.png")
            plt.show()

    except KeyboardInterrupt:
        print("사용자에 의해 중단됨")
        recorder.stop_recording()

    finally:
        print("완료")