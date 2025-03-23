import librosa
import numpy as np
import matplotlib.pyplot as plt
import sys
import os

# 부모 디렉토리 추가 (src 모듈 임포트를 위해)
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from src.audio.audio_pipeline import GuitarAudioPipeline


def analyze_chord_file(file_path, expected_chord=None):
    """
    코드 파일 분석 예제

    Args:
        file_path: 오디오 파일 경로
        expected_chord: 예상되는 코드 (검증용)
    """
    # 파이프라인 초기화
    pipeline = GuitarAudioPipeline()

    # CREPE 모델이 없으면 다운로드
    if not os.path.exists(pipeline.pitch_detector.model_path):
        pipeline.download_crepe_model(model_size="tiny")

    # 오디오 파일 분석
    print(f"\n분석 중: {file_path}")
    result = pipeline.analyze_audio_file(file_path)

    # 결과 출력
    print(f"감지된 코드: {result['detected_chord']}")
    print(f"유사도: {result['chord_similarity']:.2f}")
    print(f"감지된 음표: {', '.join(result['detected_notes'])}")
    print(f"처리 시간: {result['processing_time']:.2f}초")

    # 예상 코드가 제공된 경우 비교
    if expected_chord:
        print(f"예상 코드: {expected_chord}")
        if result['detected_chord'] == expected_chord:
            print("결과: 정확히 일치! ✓")
        else:
            print("결과: 불일치 ✗")

    # 그래프 시각화
    y, sr = librosa.load(file_path, sr=None)

    plt.figure(figsize=(12, 8))

    # 파형 그리기
    plt.subplot(2, 1, 1)
    plt.title("오디오 파형")
    plt.plot(np.linspace(0, len(y) / sr, len(y)), y)
    plt.xlabel("시간 (초)")
    plt.ylabel("진폭")

    # 스펙트로그램 그리기
    plt.subplot(2, 1, 2)
    D = librosa.amplitude_to_db(np.abs(librosa.stft(y)), ref=np.max)
    plt.title(f"스펙트로그램 - 감지된 코드: {result['detected_chord']}")
    librosa.display.specshow(D, sr=sr, x_axis='time', y_axis='log')
    plt.colorbar(format='%+2.0f dB')

    plt.tight_layout()

    # 저장 및 표시
    output_file = os.path.splitext(os.path.basename(file_path))[0] + "_analysis.png"
    plt.savefig(output_file)
    plt.show()

    return result


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="기타 코드 인식 예제")
    parser.add_argument("file_path", help="분석할 오디오 파일 경로")
    parser.add_argument("--expected", help="예상되는 코드 (검증용)", default=None)

    args = parser.parse_args()
    analyze_chord_file(args.file_path, args.expected)