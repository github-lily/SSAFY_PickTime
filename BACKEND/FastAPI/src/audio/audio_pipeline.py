import numpy as np
import librosa
import time
import os
from pathlib import Path

# 로컬 모듈 임포트
from .pitch_detection import CREPEPitchDetector
from .chord_recognition import GuitarChordRecognizer


class GuitarAudioPipeline:
    """기타 오디오 분석 전체 파이프라인"""

    def __init__(self, model_capacity="tiny", chord_db_path=None, use_advanced_models=True):
        """
        기타 오디오 분석 파이프라인 초기화

        Args:
            model_capacity: CREPE 모델 크기 ("tiny", "small", "medium", "large", "full")
            chord_db_path: 코드 데이터베이스 경로
            use_advanced_models: 고급 모델(Essentia, Magenta) 사용 여부
        """
        # 컴포넌트 초기화
        self.pitch_detector = CREPEPitchDetector(model_capacity=model_capacity)

        # 코드 인식기 초기화 - 새로운 인터페이스 사용
        if chord_db_path:
            self.chord_recognizer = GuitarChordRecognizer(chord_db_path=chord_db_path)
        else:
            self.chord_recognizer = GuitarChordRecognizer()

        # 설정
        self.min_confidence = 0.5
        self.analysis_window = 1.0  # 분석 윈도우 (초)

        # 고급 모델 초기화
        if use_advanced_models:
            # Essentia 모델 초기화
            try:
                self.chord_recognizer.initialize_essentia_models()
                print("Essentia 모델 초기화 완료")
            except Exception as e:
                print(f"Essentia 모델 초기화 실패: {e}")

            # Magenta 모델 초기화
            try:
                self.chord_recognizer.initialize_magenta_model()
                print("Magenta 모델 초기화 완료")
            except Exception as e:
                print(f"Magenta 모델 초기화 실패: {e}")

    def analyze_audio_file(self, audio_file_path):
        """
        오디오 파일 분석

        Args:
            audio_file_path: 오디오 파일 경로

        Returns:
            result: 분석 결과 딕셔너리
        """
        start_time = time.time()

        # 오디오 로드
        print(f"오디오 파일 로드 중: {audio_file_path}")
        audio, sr = librosa.load(audio_file_path, sr=None)

        # 결과
        result = self.analyze_audio(audio, sr)

        # 처리 시간 측정
        result["processing_time"] = time.time() - start_time

        return result

    def analyze_audio(self, audio, sr):
        """
        오디오 데이터 분석 (앙상블 방식)

        Args:
            audio: 오디오 데이터 (numpy array)
            sr: 샘플링 레이트

        Returns:
            result: 분석 결과 딕셔너리
        """
        # 결과 딕셔너리
        result = {
            "detected_notes": [],
            "detected_chord": "Unknown",
            "chord_similarity": 0.0,
            "confidence": 0.0
        }

        # 1. CREPE 기반 음높이 감지 (기존 방식)
        times, frequencies, confidences = self.pitch_detector.extract_pitch(audio, sr)

        # 신뢰도가 임계값 이상인 주파수만 선택
        valid_indices = confidences >= self.min_confidence
        valid_frequencies = frequencies[valid_indices]
        valid_confidences = confidences[valid_indices]

        if len(valid_frequencies) > 0:
            crepe_notes = self.chord_recognizer.extract_notes_from_frequencies(
                valid_frequencies, valid_confidences, self.min_confidence
            )
            crepe_chord, crepe_similarity = self.chord_recognizer.identify_chord(crepe_notes)
        else:
            crepe_notes = []
            crepe_chord = "Unknown"
            crepe_similarity = 0.0

        # 2. 앙상블 크로마그램 방식
        ensemble_chord, ensemble_confidence, details = self.chord_recognizer.identify_chord_with_ensemble(audio, sr)

        # 3. 최종 결과 결정 (앙상블 결과 우선)
        if ensemble_confidence > 0.5:  # 앙상블 신뢰도가 충분히 높은 경우
            detected_chord = ensemble_chord
            similarity = ensemble_confidence
        elif crepe_similarity > 0.7:  # CREPE 결과가 매우 확실한 경우
            detected_chord = crepe_chord
            similarity = crepe_similarity
        else:  # 두 결과 중 더 신뢰도가 높은 것 선택
            detected_chord = ensemble_chord if ensemble_confidence > crepe_similarity else crepe_chord
            similarity = max(ensemble_confidence, crepe_similarity)

        # 4. 후처리 적용 (코드 정확도 향상)
        stft_chroma, _ = self.chord_recognizer.extract_chroma_features(audio, sr)
        post_processed_chord = self.chord_recognizer.post_process_chord_detection(detected_chord, stft_chroma)

        # 최종 코드 선택
        final_chord = post_processed_chord if post_processed_chord != "Unknown" else detected_chord

        # 노트 추출
        if final_chord != "Unknown":
            detected_notes = self.chord_recognizer._get_chord_notes(
                chord_name=final_chord
            )
        else:
            detected_notes = crepe_notes if crepe_notes else []

        # 결과 업데이트
        result["detected_notes"] = detected_notes
        result["detected_chord"] = final_chord
        result["chord_similarity"] = similarity
        result["confidence"] = similarity

        # 상세 분석 결과 저장
        result["crepe_result"] = {
            "notes": crepe_notes,
            "chord": crepe_chord,
            "similarity": crepe_similarity
        }
        result["ensemble_result"] = details
        result["post_processed_result"] = {
            "chord": post_processed_chord
        }

        # 추가 정보
        if final_chord != "Unknown" and similarity > 0.6:
            try:
                chord_info = self._get_chord_info(final_chord)
                if chord_info:
                    result["chord_info"] = chord_info
            except Exception as e:
                print(f"코드 정보 가져오기 오류: {e}")

        return result

    def _get_chord_info(self, chord_name):
        """
        코드에 대한 간략한 이론 정보 가져오기

        Args:
            chord_name: 코드 이름

        Returns:
            dict: 코드 정보 딕셔너리
        """
        # 코드 이름에서 루트와 타입 추출
        if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
            root = chord_name[:2]
            chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
        else:
            root = chord_name[:1]
            chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

        # 기본 정보만 반환
        info = {
            "root": root,
            "type": chord_type
        }

        return info

    def analyze_audio_stream(self, audio_chunk, sr, expected_chord=None):
        """
        실시간 오디오 스트림 분석

        Args:
            audio_chunk: 오디오 데이터 청크 (numpy array)
            sr: 샘플링 레이트
            expected_chord: 예상되는 코드 (옵션)

        Returns:
            result: 분석 결과 딕셔너리
        """
        result = self.analyze_audio(audio_chunk, sr)

        # 예상 코드가 제공된 경우 정확도 평가
        if expected_chord is not None:
            if result["detected_chord"] == expected_chord:
                result["is_correct"] = True
                result["accuracy"] = result["chord_similarity"]
            else:
                result["is_correct"] = False

                # 예상 코드의 구성음 가져오기
                expected_notes = self._get_chord_notes(expected_chord)
                detected_notes_set = set(result["detected_notes"])
                expected_notes_set = set(expected_notes)

                # 누락된 음표 또는 잘못된 음표 식별
                result["missing_notes"] = list(expected_notes_set - detected_notes_set)
                result["extra_notes"] = list(detected_notes_set - expected_notes_set)

                # 부분 정확도 계산
                if len(expected_notes_set) > 0:
                    correct_notes = expected_notes_set.intersection(detected_notes_set)
                    result["accuracy"] = len(correct_notes) / len(expected_notes_set)
                else:
                    result["accuracy"] = 0.0

        return result

    def _get_chord_notes(self, chord_name=None, root=None, chord_type=None, include_full_name=True):
        """
        코드의 구성음 목록 반환

        Args:
            chord_name: 코드 이름 (예: "Cmaj7")
            root: 루트 음 (chord_name이 None일 때 사용)
            chord_type: 코드 유형 (chord_name이 None일 때 사용)
            include_full_name: 완전한 노트 이름 포함 여부 (옥타브 정보 포함)

        Returns:
            List[str]: 코드 구성음 목록
        """
        # 코드 이름이 제공된 경우 루트와 타입 추출
        if chord_name:
            if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
                root = chord_name[:2]
                chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
            else:
                root = chord_name[:1]
                chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

        # 코드 데이터베이스에서 정보 찾기
        if root in self.chord_db and chord_type in self.chord_db[root]:
            variations = self.chord_db[root][chord_type]
            if variations and "notes" in variations[0]:
                notes = variations[0]["notes"]
                return notes

        # 데이터베이스에 없으면 기본 간격 사용
        return self._generate_chord_notes(root, chord_type)