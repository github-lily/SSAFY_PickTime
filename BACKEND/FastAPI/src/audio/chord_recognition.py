import numpy as np
import json
import os
from collections import Counter
from typing import Dict, List, Tuple, Optional, Any, Union, Set

# 음 이름과 주파수 매핑 (플랫 노트 포함)
NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']
FLAT_NAMES = ['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B']

# 동일한 음에 대한 이름 매핑 (샵 -> 플랫, 플랫 -> 샵)
ENHARMONIC_MAP = {
    'C#': 'Db', 'Db': 'C#',
    'D#': 'Eb', 'Eb': 'D#',
    'F#': 'Gb', 'Gb': 'F#',
    'G#': 'Ab', 'Ab': 'G#',
    'A#': 'Bb', 'Bb': 'A#',
    'E#': 'F', 'F': 'E#',
    'B#': 'C', 'C': 'B#',
    'Cb': 'B', 'B': 'Cb'
}


class GuitarChordRecognizer:
    """기타 코드 인식 및 교육 피드백을 제공하는 클래스"""

    def __init__(self, chord_db_path="chord_database.json"):
        """
        기타 코드 인식 초기화

        Args:
            chord_db_path: 코드 데이터베이스 파일 경로
        """
        self.chord_db_path = chord_db_path
        self.chord_db = self._load_chord_database()

        # 코드 인식을 위한 간소화된 데이터베이스
        self.simple_chord_db = self._create_simple_chord_db()

        # 기본 노트 주파수 (A4 = 440Hz)
        self.A4 = 440.0

        # 기타 구성 (표준 튜닝)
        self.guitar_strings = ['E', 'A', 'D', 'G', 'B', 'E']  # 저음부터 고음 순서
        self.string_indices = {1: 0, 2: 1, 3: 2, 4: 3, 5: 4, 6: 5}  # 기타 표기법의 문자열 번호

        # 코드 난이도 캐시
        self.difficulty_cache = {}

    def _load_chord_database(self) -> Dict:
        """
        코드 데이터베이스 로드 또는 생성

        Returns:
            Dict: 로드된 또는 새로 생성된 코드 데이터베이스
        """
        if os.path.exists(self.chord_db_path):
            try:
                with open(self.chord_db_path, 'r') as f:
                    chord_db = json.load(f)
                    print(f"코드 데이터베이스 로드 완료: {len(chord_db)} 루트 노트, 다양한 코드 유형 포함")
                    return chord_db
            except Exception as e:
                print(f"코드 데이터베이스 로드 중 오류 발생: {e}")
                print("기본 코드 데이터베이스를 생성합니다.")

        # 기본 코드 데이터베이스 생성
        chord_db = self._create_basic_chord_database()

        # 디렉토리 경로 확인 및 생성
        dir_path = os.path.dirname(self.chord_db_path)
        if dir_path:
            os.makedirs(dir_path, exist_ok=True)

        # 저장
        with open(self.chord_db_path, 'w') as f:
            json.dump(chord_db, f, indent=2)

        return chord_db

    def _create_simple_chord_db(self) -> Dict:
        """
        코드 인식을 위한 간소화된 데이터베이스 생성

        Returns:
            Dict: 코드 이름을 키로, 구성음 목록을 값으로 하는 딕셔너리
        """
        simple_db = {}

        # 모든 루트 노트 순회
        for root_note, chord_types in self.chord_db.items():
            # 각 코드 유형 순회
            for chord_type, variations in chord_types.items():
                if variations and len(variations) > 0:
                    # 코드 이름 생성 (maj는 생략)
                    chord_name = f"{root_note}{chord_type}" if chord_type != "maj" else root_note

                    # 첫 번째 운지법의 notes 사용
                    if "notes" in variations[0]:
                        simple_db[chord_name] = {
                            "notes": variations[0]["notes"],
                            "root": root_note,
                            "type": chord_type
                        }

        return simple_db

    def _create_basic_chord_database(self) -> Dict:
        """
        기본 기타 코드 데이터베이스 생성 (파일이 없을 경우)

        Returns:
            Dict: 기본 코드 데이터베이스
        """
        # 매우 기본적인 코드 데이터베이스 생성
        chord_db = {}

        # 루트 음 목록 (샵과 플랫 모두 포함)
        root_notes = ['C', 'C#', 'Db', 'D', 'D#', 'Eb', 'E', 'F', 'F#',
                      'Gb', 'G', 'G#', 'Ab', 'A', 'A#', 'Bb', 'B', 'Cb']

        # 기본 코드 유형
        chord_types = [
            'maj', 'm', '7', 'm7', 'maj7', 'dim', 'dim7', 'aug',
            'sus2', 'sus4', '6', 'm6', '9', 'm9', 'maj9'
        ]

        # 간단한 데이터베이스 생성
        for root in root_notes:
            chord_db[root] = {}
            for chord_type in chord_types:
                chord_db[root][chord_type] = [
                    {
                        "notes": self._get_chord_notes(root, chord_type),
                        "root": root,
                        "type": chord_type,
                        "fingers": ["x", "x", "x", "x", "x", "x"],  # 더미 데이터
                        "structure": self._get_chord_structure(chord_type)
                    }
                ]

        return chord_db

    def _get_chord_notes(self, root: str, chord_type: str) -> List[str]:
        """
        주어진 루트와 코드 유형에 대한 구성음 생성

        Args:
            root: 루트 음 이름 (예: "C", "D#")
            chord_type: 코드 유형 (예: "maj", "m7")

        Returns:
            List[str]: 코드 구성음 목록
        """
        # 루트 음의 인덱스 찾기 (샵이나 플랫 기준)
        root_idx = -1
        if root in NOTE_NAMES:
            root_idx = NOTE_NAMES.index(root)
            note_names = NOTE_NAMES
        elif root in FLAT_NAMES:
            root_idx = FLAT_NAMES.index(root)
            note_names = FLAT_NAMES
        else:
            # 알 수 없는 루트 음인 경우 기본값
            return [root]

        # 코드 유형별 간격 매핑
        intervals = {
            'maj': [0, 4, 7],  # 메이저
            'm': [0, 3, 7],  # 마이너
            '7': [0, 4, 7, 10],  # 도미넌트 7
            'm7': [0, 3, 7, 10],  # 마이너 7
            'maj7': [0, 4, 7, 11],  # 메이저 7
            'dim': [0, 3, 6],  # 디미니쉬드
            'dim7': [0, 3, 6, 9],  # 디미니쉬드 7
            'aug': [0, 4, 8],  # 어그멘티드
            'sus2': [0, 2, 7],  # 서스펜디드 2
            'sus4': [0, 5, 7],  # 서스펜디드 4
            '6': [0, 4, 7, 9],  # 메이저 6
            'm6': [0, 3, 7, 9],  # 마이너 6
            '9': [0, 4, 7, 10, 14],  # 도미넌트 9
            'm9': [0, 3, 7, 10, 14],  # 마이너 9
            'maj9': [0, 4, 7, 11, 14],  # 메이저 9
            '11': [0, 4, 7, 10, 14, 17],  # 11
            'm11': [0, 3, 7, 10, 14, 17],  # 마이너 11
            '13': [0, 4, 7, 10, 14, 17, 21],  # 13
            'm13': [0, 3, 7, 10, 14, 17, 21],  # 마이너 13
            '7b5': [0, 4, 6, 10],  # 7 플랫 5
            '7(#5)': [0, 4, 8, 10],  # 7 샵 5 (augmented 7)
            '7(b9)': [0, 4, 7, 10, 13],  # 7 플랫 9
            '7(#9)': [0, 4, 7, 10, 15],  # 7 샵 9
            '7(#11)': [0, 4, 7, 10, 18],  # 7 샵 11
            '9b5': [0, 4, 6, 10, 14],  # 9 플랫 5
            '6/9': [0, 4, 7, 9, 14],  # 6/9
            'm7b5': [0, 3, 6, 10],  # 마이너 7 플랫 5 (half-diminished)
            'm(maj7)': [0, 3, 7, 11],  # 마이너 메이저 7
            'm(maj9)': [0, 3, 7, 11, 14],  # 마이너 메이저 9
            'add9': [0, 4, 7, 14],  # 애드 9
            '7(b13)': [0, 4, 7, 10, 20],  # 7 플랫 13
            '13(#11)': [0, 4, 7, 10, 14, 18, 21],  # 13 샵 11
            '13(#9)': [0, 4, 7, 10, 15, 17, 21],  # 13 샵 9
            '13(b9)': [0, 4, 7, 10, 13, 17, 21],  # 13 플랫 9
            'maj13': [0, 4, 7, 11, 14, 17, 21],  # 메이저 13
            '9(#11)': [0, 4, 7, 10, 14, 18],  # 9 샵 11
            '9(#5)': [0, 4, 8, 10, 14],  # 9 샵 5
            '6(#11)': [0, 4, 7, 9, 18],  # 6 샵 11
            '+(#11)': [0, 4, 8, 18]  # 어그멘티드 샵 11
        }

        # 해당 코드 유형의 간격이 없으면 기본 메이저 코드 반환
        if chord_type not in intervals:
            chord_type = 'maj'

        # 코드 구성음 생성
        notes = []
        for interval in intervals[chord_type]:
            note_idx = (root_idx + interval) % 12
            notes.append(note_names[note_idx])

        return notes

    def _get_chord_structure(self, chord_type: str) -> List[str]:
        """
        코드 유형에 대한 구조 정보 반환

        Args:
            chord_type: 코드 유형 (예: "maj", "m7")

        Returns:
            List[str]: 코드 구조 표기 (예: ["1", "3", "5"] for major)
        """
        # 코드 유형별 구조 매핑
        structures = {
            'maj': ["1", "3", "5"],
            'm': ["1", "b3", "5"],
            '7': ["1", "3", "5", "b7"],
            'm7': ["1", "b3", "5", "b7"],
            'maj7': ["1", "3", "5", "7"],
            'dim': ["1", "b3", "b5"],
            'dim7': ["1", "b3", "b5", "bb7"],
            'aug': ["1", "3", "#5"],
            'sus2': ["1", "2", "5"],
            'sus4': ["1", "4", "5"],
            '6': ["1", "3", "5", "6"],
            'm6': ["1", "b3", "5", "6"],
            '9': ["1", "3", "5", "b7", "9"],
            'm9': ["1", "b3", "5", "b7", "9"],
            'maj9': ["1", "3", "5", "7", "9"],
            '11': ["1", "3", "5", "b7", "9", "11"],
            'm11': ["1", "b3", "5", "b7", "9", "11"],
            '13': ["1", "3", "5", "b7", "9", "11", "13"],
            'm13': ["1", "b3", "5", "b7", "9", "11", "13"],
            '7b5': ["1", "3", "b5", "b7"],
            '7(#5)': ["1", "3", "#5", "b7"],
            '7(b9)': ["1", "3", "5", "b7", "b9"],
            '7(#9)': ["1", "3", "5", "b7", "#9"],
            '7(#11)': ["1", "3", "5", "b7", "#11"],
            '9b5': ["1", "3", "b5", "b7", "9"],
            '6/9': ["1", "3", "5", "6", "9"],
            'm7b5': ["1", "b3", "b5", "b7"],
            'm(maj7)': ["1", "b3", "5", "7"],
            'm(maj9)': ["1", "b3", "5", "7", "9"],
            'add9': ["1", "3", "5", "9"],
            '7(b13)': ["1", "3", "5", "b7", "b13"],
            '13(#11)': ["1", "3", "5", "b7", "9", "#11", "13"],
            '13(#9)': ["1", "3", "5", "b7", "#9", "11", "13"],
            '13(b9)': ["1", "3", "5", "b7", "b9", "11", "13"],
            'maj13': ["1", "3", "5", "7", "9", "11", "13"],
            '9(#11)': ["1", "3", "5", "b7", "9", "#11"],
            '9(#5)': ["1", "3", "#5", "b7", "9"],
            '6(#11)': ["1", "3", "5", "6", "#11"],
            '+(#11)': ["1", "3", "#5", "#11"]
        }

        # 해당 코드 유형의 구조가 없으면 기본 메이저 코드 구조 반환
        if chord_type not in structures:
            return structures['maj']

        return structures[chord_type]

    def frequency_to_note(self, frequency: float) -> Tuple[str, float]:
        """
        주파수를 음 이름으로 변환

        Args:
            frequency: Hz 단위의 주파수

        Returns:
            Tuple[str, float]: (음 이름, MIDI 노트 번호)
        """
        if frequency <= 0:
            return "N/A", 0

        # MIDI 노트 번호 계산
        midi_note = 69 + 12 * np.log2(frequency / self.A4)
        midi_note_rounded = round(midi_note)

        # 옥타브와 음 이름 계산
        octave = (midi_note_rounded // 12) - 1
        note_idx = midi_note_rounded % 12
        note_name = f"{NOTE_NAMES[note_idx]}{octave}"

        return note_name, midi_note_rounded

    def extract_notes_from_frequencies(self,
                                       frequencies: np.ndarray,
                                       confidences: np.ndarray,
                                       confidence_threshold: float = 0.5) -> List[str]:
        """
        주파수 목록에서 음표 추출

        Args:
            frequencies: 감지된 주파수 배열
            confidences: 각 주파수의 신뢰도 배열
            confidence_threshold: 유효한 주파수로 간주할 최소 신뢰도

        Returns:
            List[str]: 감지된 음표 목록 (음 이름만)
        """
        notes = []
        midi_notes = []

        for freq, conf in zip(frequencies, confidences):
            if conf >= confidence_threshold:
                note_name, midi_note = self.frequency_to_note(freq)
                if note_name != "N/A":
                    note_name_only = note_name[:-1]  # 옥타브 제거
                    notes.append(note_name_only)
                    midi_notes.append(midi_note)

        # 가장 많이 발생한 음 선택 (노이즈 제거)
        if notes:
            counter = Counter(notes)
            most_common_notes = [note for note, count in counter.most_common() if count > len(notes) * 0.1]
            return most_common_notes
        else:
            return []

    def _normalize_note_name(self, note: str) -> Set[str]:
        """
        음 이름을 표준화하고 동일한 음에 대한 모든 가능한 이름 반환

        Args:
            note: 음 이름 (예: "C#", "Db")

        Returns:
            Set[str]: 동일한 음에 대한 모든 가능한 이름 집합
        """
        enharmonics = {note}

        if note in ENHARMONIC_MAP:
            enharmonics.add(ENHARMONIC_MAP[note])

        return enharmonics

    def identify_chord(self, notes: List[str]) -> Tuple[str, float]:
        """
        감지된 음표에서 코드 식별

        Args:
            notes: 감지된 음표 목록 (음 이름만)

        Returns:
            Tuple[str, float]: (식별된 코드 이름, 유사도 점수 (0-1))
        """
        if not notes:
            return "Unknown", 0.0

        best_match = None
        best_similarity = 0.0

        # 고유한 음표만 사용하고 각 음의 이명동음(enharmonic) 표기를 포함
        unique_notes = list(set(notes))

        # 모든 가능한 이명동음 표기 포함
        all_possible_notes = set()
        for note in unique_notes:
            all_possible_notes.update(self._normalize_note_name(note))

        all_possible_notes = list(all_possible_notes)

        for chord_name, chord_info in self.simple_chord_db.items():
            chord_notes = chord_info["notes"]

            # 코드 노트의 모든 가능한 이명동음 표기 포함
            all_chord_notes = set()
            for note in chord_notes:
                all_chord_notes.update(self._normalize_note_name(note))

            # 공통 음표 수 (이명동음 고려)
            common_notes = set(all_possible_notes).intersection(all_chord_notes)

            # 유사도 계산 (Jaccard 유사도)
            union_size = len(set(all_possible_notes).union(all_chord_notes))
            if union_size > 0:
                similarity = len(common_notes) / union_size
            else:
                similarity = 0

            # 루트 음에 가중치 적용 (이명동음 고려)
            root_enharmonics = self._normalize_note_name(chord_info["root"])
            for root_note in root_enharmonics:
                if root_note in all_possible_notes:
                    similarity += 0.2
                    similarity = min(similarity, 1.0)
                    break

            if similarity > best_similarity:
                best_similarity = similarity
                best_match = chord_name

        return best_match if best_match else "Unknown", best_similarity

    def identify_chord_with_positions(self,
                                      notes: List[str],
                                      fretboard_positions: Optional[Dict[str, Dict]] = None) -> Dict:
        """
        감지된 음표와 프렛보드 위치에서 코드 식별 및 교육 정보 제공

        Args:
            notes: 감지된 음표 목록 (음 이름만)
            fretboard_positions: 옵션으로 제공되는 프렛보드 위치 정보
                                {손가락: {"string": 문자열 번호, "fret": 프렛 번호}}

        Returns:
            Dict: 식별된 코드 정보, 정확도, 피드백 등을 포함한 딕셔너리
        """
        # 기본 코드 식별
        chord_name, similarity = self.identify_chord(notes)

        result = {
            "chord_name": chord_name,
            "confidence": similarity * 100,  # 백분율로 변환
            "detected_notes": notes
        }

        # 코드를 인식하지 못했으면 여기서 종료
        if chord_name == "Unknown" or similarity < 0.5:
            return result

        # 코드 유형과 루트 노트 추출
        if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
            root = chord_name[:2]
            chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
        else:
            root = chord_name[:1]
            chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

        # 전체 데이터베이스에서 해당 코드 정보 검색
        if root in self.chord_db and chord_type in self.chord_db[root]:
            chord_variations = self.chord_db[root][chord_type]
            result["variations_count"] = len(chord_variations)

            # 가능한 운지법 추가
            result["fingerings"] = []
            for variation in chord_variations:
                result["fingerings"].append({
                    "fingers": variation.get("fingers", []),
                    "notes": variation.get("notes", []),
                    "structure": variation.get("structure", [])
                })

            # 프렛보드 위치 정보가 제공된 경우, 최적의 운지법 매칭 및 피드백 제공
            if fretboard_positions:
                best_match, accuracy, feedback = self._match_position_to_fingering(
                    fretboard_positions, chord_variations
                )

                if best_match is not None:
                    result["best_matching_position"] = best_match
                    result["playing_accuracy"] = accuracy
                    result["feedback"] = feedback

        return result

    def _match_position_to_fingering(self,
                                     positions: Dict[str, Dict],
                                     variations: List[Dict]) -> Tuple[Optional[int], float, List[str]]:
        """
        감지된 손가락 위치를 가능한 운지법과 매칭

        Args:
            positions: 감지된 손가락 위치 {손가락: {"string": 문자열 번호, "fret": 프렛 번호}}
            variations: 해당 코드의 가능한 운지법 변형 목록

        Returns:
            Tuple: (최적 매칭 변형 인덱스, 정확도 점수, 피드백 메시지 목록)
        """
        best_match_idx = None
        best_accuracy = 0.0
        best_feedback = []

        for i, variation in enumerate(variations):
            if "fingers" not in variation:
                continue

            fingers = variation["fingers"]
            accuracy, feedback = self._calculate_fingering_accuracy(positions, fingers)

            if accuracy > best_accuracy:
                best_accuracy = accuracy
                best_match_idx = i
                best_feedback = feedback

        return best_match_idx, best_accuracy, best_feedback

    def _calculate_fingering_accuracy(self,
                                      positions: Dict[str, Dict],
                                      fingering: List[str]) -> Tuple[float, List[str]]:
        """
        감지된 손가락 위치와 표준 운지법 간의 정확도 계산

        Args:
            positions: 감지된 손가락 위치 {손가락: {"string": 문자열 번호, "fret": 프렛 번호}}
            fingering: 표준 운지법 (예: ["x", "3", "2", "0", "1", "0"])

        Returns:
            Tuple: (정확도 점수 (0-1), 피드백 메시지 목록)
        """
        feedback = []
        correct_positions = 0
        total_positions = 0

        # 운지법 비교를 위한 포지션 매핑
        detected_strings = {}
        for finger, data in positions.items():
            if "string" in data and "fret" in data:
                string_idx = self.string_indices.get(data["string"], -1)
                if string_idx >= 0:
                    detected_strings[string_idx] = data["fret"]

        # 표준 운지법과 비교
        for i, fret_pos in enumerate(fingering):
            if fret_pos == "x":  # 뮤트된 문자열
                if i in detected_strings:
                    feedback.append(f"{i + 1}번 줄은 연주하지 않아야 합니다.")
                continue

            total_positions += 1

            if i not in detected_strings:
                feedback.append(f"{i + 1}번 줄을 연주해야 합니다.")
                continue

            expected_fret = int(fret_pos) if fret_pos != "0" else 0
            actual_fret = detected_strings[i]

            if expected_fret == actual_fret:
                correct_positions += 1
            else:
                feedback.append(f"{i + 1}번 줄은 {expected_fret}번 프렛이어야 합니다. (현재: {actual_fret}번)")

        # 정확도 계산
        accuracy = correct_positions / max(1, total_positions)

        # 전반적인 피드백 추가
        if accuracy == 1.0:
            feedback.insert(0, "완벽하게 연주했습니다!")
        elif accuracy >= 0.8:
            feedback.insert(0, "거의 정확합니다. 몇 가지 작은 조정이 필요합니다.")
        elif accuracy >= 0.5:
            feedback.insert(0, "기본적인 코드 모양은 맞지만, 몇 가지 수정이 필요합니다.")
        else:
            feedback.insert(0, "코드 포지션을 다시 확인해보세요.")

        return accuracy, feedback

    def get_chord_for_learning(self, difficulty: str = 'beginner') -> Dict:
        """
        학습용 코드 추천

        Args:
            difficulty: 난이도 ('beginner', 'intermediate', 'advanced')

        Returns:
            Dict: 학습용 코드 정보
        """
        # 난이도별 추천 코드
        if difficulty == 'beginner':
            # 초보자용 기본 코드
            beginner_chords = ['C', 'G', 'D', 'A', 'E', 'Am', 'Em', 'Dm']
            import random
            chord_name = random.choice(beginner_chords)
        elif difficulty == 'intermediate':
            # 중급자용 코드
            intermediate_chords = ['F', 'B', 'Bm', 'C7', 'G7', 'Fmaj7', 'Cmaj7']
            import random
            chord_name = random.choice(intermediate_chords)
        else:
            # 고급 코드
            advanced_chords = ['Dmaj9', 'Caug', 'F#m7b5', 'Bm7b5', 'Esus4', 'A13', 'Bbdim7']
            import random
            chord_name = random.choice(advanced_chords)

        # 코드 정보 가져오기
        if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
            root = chord_name[:2]
            chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
        else:
            root = chord_name[:1]
            chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

        # 해당 코드가 데이터베이스에 있는지 확인
        if root in self.chord_db and chord_type in self.chord_db[root]:
            variations = self.chord_db[root][chord_type]
            if variations:
                variation = variations[0]  # 첫 번째 변형 사용
                return {
                    "chord_name": chord_name,
                    "root": root,
                    "type": chord_type,
                    "fingers": variation.get("fingers", []),
                    "notes": variation.get("notes", []),
                    "structure": variation.get("structure", [])
                }

        # 데이터베이스에 없으면 기본 정보 반환
        return {
            "chord_name": chord_name,
            "root": root,
            "type": chord_type if chord_type != "maj" else "",
            "notes": self._get_chord_notes(root, chord_type)
        }

    def compare_with_target_chord(self,
                                  finger_positions: Dict[str, Dict],
                                  target_chord: Dict) -> Dict:
        """
        감지된 손가락 위치와 목표 코드 비교 (학습 모드)

        Args:
            finger_positions: 감지된 손가락 위치 정보
            target_chord: 학습 목표 코드 정보

        Returns:
            Dict: 비교 결과 및 피드백
        """
        chord_name = target_chord.get("chord_name", "Unknown")
        root = target_chord.get("root", "")
        chord_type = target_chord.get("type", "")

        # 데이터베이스에서 해당 코드 찾기
        if root in self.chord_db and chord_type in self.chord_db[root]:
            variations = self.chord_db[root][chord_type]

            # 운지법과 비교
            best_match_idx, accuracy, feedback = self._match_position_to_fingering(
                finger_positions, variations
            )

            result = {
                "chord_name": chord_name,
                "accuracy": accuracy * 100,  # 백분율로 변환
                "feedback": feedback,
                "is_correct": accuracy >= 0.8  # 80% 이상 정확하면 정확하다고 간주
            }

            # 정확한 운지법 제안
            if best_match_idx is not None and best_match_idx < len(variations):
                correct_fingering = variations[best_match_idx].get("fingers", [])
                result["correct_fingering"] = correct_fingering

                # 오류가 있는 경우 교정 제안
                if accuracy < 1.0:
                    correction_tips = self._generate_correction_tips(finger_positions, correct_fingering)
                    result["correction_tips"] = correction_tips

            return result

        # 데이터베이스에 없는 경우
        return {
            "chord_name": chord_name,
            "accuracy": 0.0,
            "feedback": ["해당 코드의 정보가 데이터베이스에 없습니다."],
            "is_correct": False
        }

    def _generate_correction_tips(self,
                                  positions: Dict[str, Dict],
                                  correct_fingering: List[str]) -> List[str]:
        """
        손가락 위치 교정을 위한 팁 생성

        Args:
            positions: 감지된 손가락 위치
            correct_fingering: 올바른 운지법

        Returns:
            List[str]: 교정 팁 목록
        """
        tips = []

        # 운지법 비교를 위한 포지션 매핑
        detected_strings = {}
        for finger, data in positions.items():
            if "string" in data and "fret" in data:
                string_idx = self.string_indices.get(data["string"], -1)
                if string_idx >= 0:
                    detected_strings[string_idx] = {
                        "fret": data["fret"],
                        "finger": finger
                    }

        # 각 줄에 대한 교정 팁
        for i, fret_pos in enumerate(correct_fingering):
            if fret_pos == "x":  # 뮤트된 문자열
                if i in detected_strings:
                    tips.append(f"{i + 1}번 줄은 연주하지 않아야 합니다.")
                continue

            expected_fret = int(fret_pos) if fret_pos != "0" else 0

            if i not in detected_strings:
                tips.append(f"{i + 1}번 줄의 {expected_fret}번 프렛을 연주해야 합니다.")
                continue

            actual_fret = detected_strings[i]["fret"]
            finger = detected_strings[i]["finger"]

            if expected_fret != actual_fret:
                tips.append(f"{finger} 손가락을 {i + 1}번 줄의 {expected_fret}번 프렛으로 이동하세요. (현재: {actual_fret}번)")

        # 자세에 대한 일반적인 팁 추가
        tips.append("손가락을 프렛에 가깝게 놓고, 깔끔한 소리를 위해 충분한 압력을 가하세요.")
        tips.append("엄지는 넥 뒤쪽에 자연스럽게 위치시켜 안정감을 주세요.")

        return tips

    def get_alternative_fingerings(self, chord_name: str, difficulty: str = 'all') -> List[Dict]:
        """
        특정 코드의 대체 운지법 제공

        Args:
            chord_name: 코드 이름 (예: "C", "Am7")
            difficulty: 운지법 난이도 필터 ('easy', 'medium', 'hard', 'all')

        Returns:
            List[Dict]: 대체 운지법 목록
        """
        alternatives = []

        # 코드 이름에서 루트와 타입 추출
        if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
            root = chord_name[:2]
            chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
        else:
            root = chord_name[:1]
            chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

        # 데이터베이스에서 해당 코드 검색
        if root in self.chord_db and chord_type in self.chord_db[root]:
            variations = self.chord_db[root][chord_type]

            for i, variation in enumerate(variations):
                # 난이도 평가
                var_difficulty = self._assess_fingering_difficulty(variation.get("fingers", []))

                # 난이도 필터링
                if difficulty != 'all' and var_difficulty != difficulty:
                    continue

                alternatives.append({
                    "index": i,
                    "fingers": variation.get("fingers", []),
                    "notes": variation.get("notes", []),
                    "difficulty": var_difficulty
                })

        return alternatives

    def _assess_fingering_difficulty(self, fingering: List[str]) -> str:
        """
        운지법의 난이도 평가

        Args:
            fingering: 운지법 표기 (예: ["x", "3", "2", "0", "1", "0"])

        Returns:
            str: 난이도 ('easy', 'medium', 'hard')
        """
        if not fingering:
            return 'easy'

        # 난이도 지표 계산
        barres = 0  # 바레코드 필요 여부
        spread = 0  # 손가락 간격
        positions = []

        for pos in fingering:
            if pos != "x" and pos != "0":
                positions.append(int(pos))

        # 바레코드 감지 (같은 프렛에 여러 손가락)
        if positions:
            pos_counter = Counter(positions)
            for pos, count in pos_counter.items():
                if count >= 3:  # 3개 이상의 문자열이 같은 프렛에 있으면 바레로 간주
                    barres += 1

        # 손가락 간격 계산
        if len(positions) >= 2:
            spread = max(positions) - min(positions)

        # 난이도 평가
        if barres == 0 and spread <= 2 and max(positions, default=0) <= 3:
            return 'easy'
        elif barres <= 1 and spread <= 4 and max(positions, default=0) <= 5:
            return 'medium'
        else:
            return 'hard'

    def get_chord_progression_for_song(self, style: str, key: str, complexity: str = 'simple') -> List[Dict]:
        """
        노래에 사용할 수 있는 코드 진행 추천

        Args:
            style: 음악 스타일 ('pop', 'rock', 'jazz', 'blues', 'folk')
            key: 음악 키 (예: 'C', 'Am')
            complexity: 복잡성 ('simple', 'medium', 'complex')

        Returns:
            List[Dict]: 코드 진행 정보 리스트
        """
        # 키가 메이저인지 마이너인지 확인
        is_minor = key.endswith('m')
        root = key[:-1] if is_minor else key

        # 스타일별 코드 진행 패턴
        progressions = {
            'pop': {
                'simple': {
                    'major': ['I', 'V', 'vi', 'IV'],
                    'minor': ['i', 'VI', 'III', 'VII']
                },
                'medium': {
                    'major': ['I', 'vi', 'IV', 'V', 'I', 'IV', 'V'],
                    'minor': ['i', 'VII', 'VI', 'V', 'i', 'iv', 'V']
                },
                'complex': {
                    'major': ['I', 'V', 'vi', 'iii', 'IV', 'I', 'IV', 'V'],
                    'minor': ['i', 'v', 'VI', 'III', 'iv', 'i', 'V', 'i']
                }
            },
            'rock': {
                'simple': {
                    'major': ['I', 'IV', 'V'],
                    'minor': ['i', 'VII', 'VI']
                },
                'medium': {
                    'major': ['I', 'IV', 'V', 'IV'],
                    'minor': ['i', 'VI', 'VII', 'i']
                },
                'complex': {
                    'major': ['I', 'V', 'vi', 'IV', 'I', 'V', 'IV', 'V'],
                    'minor': ['i', 'VII', 'VI', 'VII', 'i', 'v', 'VI', 'VII']
                }
            },
            'jazz': {
                'simple': {
                    'major': ['IIm7', 'V7', 'Imaj7'],
                    'minor': ['IIm7b5', 'V7', 'Im7']
                },
                'medium': {
                    'major': ['Imaj7', 'VIm7', 'IIm7', 'V7', 'IIIm7', 'VIm7', 'II7', 'V7'],
                    'minor': ['Im7', 'IVm7', 'VIImaj7', 'III7', 'VIm7b5', 'II7', 'V7', 'Im7']
                },
                'complex': {
                    'major': ['Imaj7', 'VIm7', 'IIm7', 'V7alt', 'IIIm7', 'VI7', 'IIm7', 'V7b9'],
                    'minor': ['Im7', 'VIIm7b5', 'III7', 'VIm7', 'IIm7b5', 'V7alt', 'Im7', 'V7b9']
                }
            },
            'blues': {
                'simple': {
                    'major': ['I7', 'IV7', 'I7', 'I7', 'IV7', 'IV7', 'I7', 'I7', 'V7', 'IV7', 'I7', 'V7'],
                    'minor': ['Im7', 'IVm7', 'Im7', 'Im7', 'IVm7', 'IVm7', 'Im7', 'Im7', 'V7', 'IVm7', 'Im7', 'V7']
                },
                'medium': {
                    'major': ['I7', 'IV7', 'I7', 'I7', 'IV7', 'IV7', 'I7', 'I7', 'V7', 'IV7', 'I7', 'V7', 'I7'],
                    'minor': ['Im7', 'IVm7', 'Im7', 'Im7', 'IVm7', 'IVm7', 'Im7', 'Im7', 'V7', 'IVm7', 'Im7', 'V7',
                              'Im7']
                },
                'complex': {
                    'major': ['I7', 'IV7', 'I7', 'I7', 'IV7', 'IVm7', 'I7', 'VI7', 'II7', 'V7', 'I7', 'V7'],
                    'minor': ['Im7', 'IVm7', 'Im7', 'Im7', 'IVm7', 'IV7', 'Im7', 'VIm7b5', 'II7b9', 'V7', 'Im7',
                              'V7alt']
                }
            },
            'folk': {
                'simple': {
                    'major': ['I', 'IV', 'I', 'V'],
                    'minor': ['i', 'iv', 'i', 'V']
                },
                'medium': {
                    'major': ['I', 'vi', 'ii', 'V', 'I', 'IV', 'I', 'V'],
                    'minor': ['i', 'VI', 'iv', 'V', 'i', 'iv', 'i', 'V']
                },
                'complex': {
                    'major': ['I', 'iii', 'vi', 'IV', 'ii', 'V', 'I', 'I7', 'IV', 'IVm', 'I', 'V7'],
                    'minor': ['i', 'VII', 'III', 'VI', 'ii°', 'V', 'i', 'i7', 'iv', 'IV', 'i', 'V7']
                }
            }
        }

        # 기본 스타일 설정
        if style not in progressions:
            style = 'pop'

        # 복잡성 설정
        if complexity not in ['simple', 'medium', 'complex']:
            complexity = 'simple'

        # 모드 설정
        mode = 'minor' if is_minor else 'major'

        # 해당 스타일, 복잡성, 모드의 코드 진행 가져오기
        roman_numerals = progressions[style][complexity][mode]

        # 코드 변환
        return self._convert_roman_progression(roman_numerals, root, mode)

    def _convert_roman_progression(self, roman_numerals: List[str], key: str, mode: str) -> List[Dict]:
        """
        로마 숫자 코드 진행을 실제 코드 목록으로 변환

        Args:
            roman_numerals: 로마 숫자 코드 목록
            key: 키 (예: 'C', 'G')
            mode: 모드 ('major' 또는 'minor')

        Returns:
            List[Dict]: 코드 정보 목록
        """
        result = []

        for roman in roman_numerals:
            # 코드 품질 추출 (7, maj7, m7 등)
            chord_quality = ''.join([c for c in roman if not c.isalpha() or c.islower()])

            # 로마 숫자만 추출
            roman_only = ''.join([c for c in roman if c.isalpha() and c.isupper()])

            # 코드 유형 (마이너인 경우 소문자 로마 숫자)
            is_minor = roman[0].islower() if roman else False

            # 로마 숫자를 실제 코드로 변환
            chord_name = self._roman_to_chord(roman_only, key, mode, is_minor, chord_quality)

            # 루트와 타입 분리
            if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
                root = chord_name[:2]
                chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
            else:
                root = chord_name[:1]
                chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

            # 코드 정보 생성
            chord_info = {
                "chord_name": chord_name,
                "root": root,
                "type": chord_type,
                "roman": roman
            }

            # 운지법 정보 추가 (데이터베이스에 있으면)
            if root in self.chord_db and chord_type in self.chord_db[root]:
                variations = self.chord_db[root][chord_type]
                if variations:
                    # 첫 번째 운지법 사용
                    variation = variations[0]
                    chord_info["fingers"] = variation.get("fingers", [])
                    chord_info["notes"] = variation.get("notes", [])
                    chord_info["structure"] = variation.get("structure", [])

            result.append(chord_info)

        return result

    def _roman_to_chord(self, roman: str, key: str, mode: str, is_minor: bool, quality: str) -> str:
        """
        로마 숫자 코드를 실제 코드로 변환

        Args:
            roman: 로마 숫자 (예: 'I', 'IV', 'V')
            key: 키 (예: 'C', 'G')
            mode: 모드 ('major' 또는 'minor')
            is_minor: 마이너 코드 여부
            quality: 코드 품질 (예: '7', 'maj7', 'm7')

        Returns:
            str: 변환된 코드 이름
        """
        # 로마 숫자 매핑
        roman_to_degree = {
            'I': 0, 'II': 1, 'III': 2, 'IV': 3, 'V': 4, 'VI': 5, 'VII': 6
        }

        # 로마 숫자에서 음계 단계 추출
        degree = roman_to_degree.get(roman, 0)

        # 메이저 키의 간격
        major_intervals = [0, 2, 4, 5, 7, 9, 11]

        # 마이너 키의 간격 (자연 마이너 스케일)
        minor_intervals = [0, 2, 3, 5, 7, 8, 10]

        # 현재 모드의 간격 선택
        intervals = minor_intervals if mode == 'minor' else major_intervals

        # 루트 음의 인덱스 찾기
        if key in NOTE_NAMES:
            root_idx = NOTE_NAMES.index(key)
            note_names = NOTE_NAMES
        elif key in FLAT_NAMES:
            root_idx = FLAT_NAMES.index(key)
            note_names = FLAT_NAMES
        else:
            # 기본값
            root_idx = 0
            note_names = NOTE_NAMES

        # 코드 루트 계산
        chord_root_idx = (root_idx + intervals[degree]) % 12
        chord_root = note_names[chord_root_idx]

        # 코드 품질 매핑
        chord_qualities = {
            'major': ['', 'm', 'm', '', '', 'm', 'dim'],  # 메이저 키의 기본 코드 품질
            'minor': ['m', 'dim', '', 'm', 'm', '', '']  # 마이너 키의 기본 코드 품질
        }

        # 코드 품질 결정
        base_quality = chord_qualities[mode][degree]

        # 강제 마이너/메이저 적용
        if is_minor and base_quality == '':
            base_quality = 'm'
        elif not is_minor and base_quality == 'm':
            base_quality = ''

        # 추가적인 품질 적용 (7, maj7 등)
        final_quality = quality if quality else base_quality

        # 품질 조합
        if final_quality:
            # 마이너 7th와 같은 특수 케이스 처리
            if 'm' in base_quality and '7' in quality and 'maj7' not in quality:
                final_quality = 'm7'
            elif 'm' in base_quality and 'maj7' in quality:
                final_quality = 'm(maj7)'
            elif 'dim' in base_quality and '7' in quality:
                final_quality = 'dim7'
            elif base_quality and quality and quality[0] not in ['7', 'm', '(']:
                final_quality = base_quality + quality

        # 최종 코드 이름 반환
        return chord_root + final_quality


# 코드 인식 유틸리티 함수
def recognize_chord(finger_mappings: Dict[str, Dict], chord_db: Dict) -> Dict:
    """
    손가락 매핑에서 코드 인식

    Args:
        finger_mappings: 프렛보드에 매핑된 손가락 정보
        chord_db: 코드 데이터베이스

    Returns:
        Dict: 인식된 코드 정보
    """
    # GuitarChordRecognizer 인스턴스 생성
    recognizer = GuitarChordRecognizer()

    # 손가락 위치에서 노트 추출 (여기서는 간단한 로직 사용)
    detected_notes = []
    for finger, data in finger_mappings.items():
        if isinstance(data, dict) and "string" in data and "fret" in data:
            string_idx = data["string"] - 1  # 1-indexed에서 0-indexed로 변환
            if 0 <= string_idx < len(recognizer.guitar_strings):
                base_note = recognizer.guitar_strings[string_idx]
                note_idx = (NOTE_NAMES.index(base_note[0]) + data["fret"]) % 12
                detected_notes.append(NOTE_NAMES[note_idx])

    # 코드 인식 및 교육 정보 포함
    chord_info = recognizer.identify_chord_with_positions(detected_notes, finger_mappings)

    return chord_info


def compare_with_target_chord(finger_mappings: Dict[str, Dict], target_chord: Dict) -> Dict:
    """
    손가락 매핑과 목표 코드 비교 (학습 모드용)

    Args:
        finger_mappings: 프렛보드에 매핑된 손가락 정보
        target_chord: 학습 목표 코드 정보

    Returns:
        Dict: 비교 결과 및 피드백
    """
    # GuitarChordRecognizer 인스턴스 생성
    recognizer = GuitarChordRecognizer()

    # 인식기를 사용하여 비교 수행
    result = recognizer.compare_with_target_chord(finger_mappings, target_chord)

    return result


def get_chord_for_learning(level: str = 'beginner', chord_db: Optional[Dict] = None) -> Dict:
    """
    학습용 코드 추천

    Args:
        level: 학습 난이도 ('beginner', 'intermediate', 'advanced')
        chord_db: 코드 데이터베이스 (옵션)

    Returns:
        Dict: 추천된 코드 정보
    """
    recognizer = GuitarChordRecognizer()
    return recognizer.get_chord_for_learning(level)


def get_chord_progression(style: str = 'pop', key: str = 'C', complexity: str = 'simple') -> List[Dict]:
    """
    특정 스타일과 키에 맞는 코드 진행 추천

    Args:
        style: 음악 스타일 ('pop', 'rock', 'jazz', 'blues', 'folk')
        key: 음악 키 (예: 'C', 'G', 'Am')
        complexity: 복잡성 ('simple', 'medium', 'complex')

    Returns:
        List[Dict]: 추천된 코드 진행 정보
    """
    recognizer = GuitarChordRecognizer()
    return recognizer.get_chord_progression_for_song(style, key, complexity)


# 시각화 관련 유틸리티 함수
def visualize_chord_diagram(chord_name: str, size: Tuple[int, int] = (400, 600)) -> np.ndarray:
    """
    코드 다이어그램 시각화

    Args:
        chord_name: 코드 이름
        size: 이미지 크기 (가로, 세로)

    Returns:
        np.ndarray: 코드 다이어그램 이미지
    """
    # 이 함수는 실제 구현이 필요합니다. 여기서는 기본 구조만 제공합니다.
    # OpenCV나 PIL 같은 라이브러리를 사용해 구현할 수 있습니다.

    # 예제 구현
    import numpy as np
    import cv2 as cv

    # 코드 인식기 초기화
    recognizer = GuitarChordRecognizer()

    # 코드 정보 파싱
    if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
        root = chord_name[:2]
        chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
    else:
        root = chord_name[:1]
        chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

    # 기본 이미지 생성
    image = np.ones((size[1], size[0], 3), dtype=np.uint8) * 255

    # 프렛보드 그리기
    fretboard_height = size[1] * 0.6
    fretboard_width = size[0] * 0.8
    fretboard_top = size[1] * 0.15
    fretboard_left = size[0] * 0.1

    # 프렛보드 배경
    cv.rectangle(
        image,
        (int(fretboard_left), int(fretboard_top)),
        (int(fretboard_left + fretboard_width), int(fretboard_top + fretboard_height)),
        (220, 190, 150),  # 나무 색상
        -1
    )

    # 문자열 그리기
    string_count = 6
    string_spacing = fretboard_height / (string_count - 1)
    for i in range(string_count):
        y = int(fretboard_top + i * string_spacing)
        cv.line(
            image,
            (int(fretboard_left), y),
            (int(fretboard_left + fretboard_width), y),
            (100, 100, 100),
            2
        )

    # 프렛 그리기
    fret_count = 5
    fret_spacing = fretboard_width / fret_count
    for i in range(fret_count + 1):
        x = int(fretboard_left + i * fret_spacing)
        cv.line(
            image,
            (x, int(fretboard_top)),
            (x, int(fretboard_top + fretboard_height)),
            (50, 50, 50),
            2 if i == 0 else 1  # 첫 번째 프렛(넛)은 더 두껍게
        )

    # 코드 정보 추가
    cv.putText(
        image,
        chord_name,
        (int(size[0] / 2 - 80), 40),
        cv.FONT_HERSHEY_SIMPLEX,
        1.5,
        (0, 0, 0),
        3
    )

    # 데이터베이스에서 운지법 찾기
    fingering = None
    if root in recognizer.chord_db and chord_type in recognizer.chord_db[root]:
        variations = recognizer.chord_db[root][chord_type]
        if variations and "fingers" in variations[0]:
            fingering = variations[0]["fingers"]

    # 운지법이 있으면 표시
    if fingering:
        string_labels = ["E", "A", "D", "G", "B", "E"]
        for i, finger_pos in enumerate(fingering):
            if i < string_count:
                # 문자열 레이블 표시
                cv.putText(
                    image,
                    string_labels[i],
                    (int(fretboard_left - 25), int(fretboard_top + i * string_spacing + 5)),
                    cv.FONT_HERSHEY_SIMPLEX,
                    0.5,
                    (0, 0, 0),
                    1
                )

                if finger_pos == "x":
                    # X 표시 (뮤트)
                    cv.putText(
                        image,
                        "X",
                        (int(fretboard_left - 15), int(fretboard_top + i * string_spacing + 5)),
                        cv.FONT_HERSHEY_SIMPLEX,
                        0.6,
                        (0, 0, 150),  # 빨간색
                        2
                    )
                elif finger_pos == "0":
                    # 오픈 문자열
                    cv.putText(
                        image,
                        "O",
                        (int(fretboard_left - 15), int(fretboard_top + i * string_spacing + 5)),
                        cv.FONT_HERSHEY_SIMPLEX,
                        0.6,
                        (0, 150, 0),  # 녹색
                        2
                    )
                else:
                    # 손가락 위치 표시
                    fret_pos = int(finger_pos)
                    if 1 <= fret_pos <= fret_count:
                        x = int(fretboard_left + (fret_pos - 0.5) * fret_spacing)
                        y = int(fretboard_top + i * string_spacing)
                        cv.circle(
                            image,
                            (x, y),
                            10,
                            (0, 0, 150),
                            -1
                        )

    return image


def calculate_chord_difficulty(chord_name: str) -> Dict:
    """
    코드의 난이도 계산

    Args:
        chord_name: 코드 이름

    Returns:
        Dict: 난이도 정보 {'difficulty': 'easy/medium/hard', 'reason': '이유'}
    """
    recognizer = GuitarChordRecognizer()

    # 코드 이름 파싱
    if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
        root = chord_name[:2]
        chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
    else:
        root = chord_name[:1]
        chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

    # 데이터베이스에서 운지법 찾기
    if root in recognizer.chord_db and chord_type in recognizer.chord_db[root]:
        variations = recognizer.chord_db[root][chord_type]
        if variations and "fingers" in variations[0]:
            fingering = variations[0]["fingers"]
            difficulty = recognizer._assess_fingering_difficulty(fingering)

            # 난이도 이유 설명
            reason = ""
            if difficulty == 'easy':
                reason = "쉬운 운지법으로 초보자도 연주할 수 있습니다."
            elif difficulty == 'medium':
                reason = "중간 난이도의 운지법으로 약간의 연습이 필요합니다."
            else:
                reason = "복잡한 운지법으로 바레 코드 또는 넓은 손가락 간격이 필요합니다."

            return {
                'difficulty': difficulty,
                'reason': reason,
                'fingering': fingering
            }

    # 데이터베이스에 없는 경우
    return {
        'difficulty': 'unknown',
        'reason': "코드 데이터베이스에서 해당 코드의 운지법을 찾을 수 없습니다.",
        'fingering': None
    }


def get_chord_theory_info(chord_name: str) -> Dict:
    """
    코드의 이론적 정보 제공

    Args:
        chord_name: 코드 이름

    Returns:
        Dict: 코드 이론 정보
    """
    recognizer = GuitarChordRecognizer()

    # 코드 이름 파싱
    if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
        root = chord_name[:2]
        chord_type = chord_name[2:] if len(chord_name) > 2 else "maj"
    else:
        root = chord_name[:1]
        chord_type = chord_name[1:] if len(chord_name) > 1 else "maj"

    # 이론 정보
    theory_info = {
        'chord_name': chord_name,
        'root': root,
        'type': chord_type,
        'notes': [],
        'structure': [],
        'description': '',
        'common_uses': []
    }

    # 데이터베이스에서 정보 찾기
    if root in recognizer.chord_db and chord_type in recognizer.chord_db[root]:
        variations = recognizer.chord_db[root][chord_type]
        if variations:
            # 노트 및 구조 정보
            if "notes" in variations[0]:
                theory_info['notes'] = variations[0]["notes"]

            if "structure" in variations[0]:
                theory_info['structure'] = variations[0]["structure"]

    # 코드 유형 설명
    descriptions = {
        'maj': f"{root} 메이저 코드는 가장 기본적인 코드로, 밝고 안정적인 소리가 특징입니다.",
        'm': f"{root} 마이너 코드는 슬프거나 어두운 느낌을 주는 코드입니다.",
        '7': f"{root}7 코드는 블루스와 재즈에서 많이 사용되는 도미넌트 7th 코드입니다.",
        'maj7': f"{root}maj7 코드는 부드럽고 세련된 느낌을 주는 메이저 7th 코드입니다.",
        'm7': f"{root}m7 코드는 부드럽고 어두운 느낌을 주는 마이너 7th 코드입니다.",
        'dim': f"{root}dim 코드는 긴장감 있는 소리가 특징인 디미니쉬드 코드입니다.",
        'aug': f"{root}aug 코드는 불안정하고 신비로운 느낌을 주는 어그멘티드 코드입니다.",
        'sus4': f"{root}sus4 코드는 불확정적인 느낌을 주는 서스펜디드 4th 코드입니다.",
        'sus2': f"{root}sus2 코드는 밝고 열린 느낌을 주는 서스펜디드 2nd 코드입니다."
    }

    # 코드 사용 예시
    common_uses = {
        'maj': ["대부분의 팝, 록, 포크 음악에서 주요 코드로 사용됩니다.", "안정적인 느낌을 주어 곡의 시작과 끝에 자주 사용됩니다."],
        'm': ["슬픈 분위기의 곡이나 발라드에서 많이 사용됩니다.", "마이너 키의 곡에서 으뜸화음으로 사용됩니다."],
        '7': ["블루스 음악의 핵심 요소입니다.", "다음 코드로 강한 해결감을 주는 진행에 사용됩니다."],
        'maj7': ["재즈, 보사노바, R&B 등 세련된 음악에서 자주 사용됩니다.", "부드러운 분위기의 발라드에 적합합니다."],
        'm7': ["재즈, 소울, R&B 등에서 자주 사용되는 코드입니다.", "부드러운 진행감을 만들어내는 데 효과적입니다."],
        'dim': ["불안정한 느낌을 주어 경과구나 브릿지에서 사용됩니다.", "다음 코드로의 강한 진행감을 만들어냅니다."],
        'aug': ["긴장감을 고조시키는 부분에서 사용됩니다.", "경과적 화음으로 사용되어 색다른 느낌을 줍니다."],
        'sus4': ["보통 메이저나 마이너 코드 전에 경과적으로 사용됩니다.", "록 음악에서 특유의 열린 소리로 자주 사용됩니다."],
        'sus2': ["현대 팝 음악에서 밝은 분위기를 위해 자주 사용됩니다.", "메이저 코드 대신 사용하여 색다른 느낌을 줍니다."]
    }

    # 설명 및 사용 예시 추가
    if chord_type in descriptions:
        theory_info['description'] = descriptions[chord_type]
    else:
        theory_info['description'] = f"{chord_name} 코드는 복잡한 화성적 특성을 가지고 있습니다."

    if chord_type in common_uses:
        theory_info['common_uses'] = common_uses[chord_type]
    else:
        theory_info['common_uses'] = ["재즈나 현대 음악에서 복잡한 화성 진행에 사용됩니다."]

    return theory_info


def suggest_songs_with_chord(chord_name: str) -> List[Dict]:
    """
    특정 코드를 포함하는 곡 추천

    Args:
        chord_name: 코드 이름

    Returns:
        List[Dict]: 추천 곡 목록
    """
    # 이 함수는 실제 구현에서는 데이터베이스나 API를 통해 정보를 가져와야 합니다.
    # 여기서는 예시 데이터만 제공합니다.

    # 일반적인 코드별 유명 곡 목록
    chord_songs = {
        'C': [
            {"title": "Let It Be", "artist": "The Beatles", "difficulty": "easy"},
            {"title": "Wonderful Tonight", "artist": "Eric Clapton", "difficulty": "medium"},
            {"title": "Viva La Vida", "artist": "Coldplay", "difficulty": "medium"}
        ],
        'G': [
            {"title": "Sweet Home Alabama", "artist": "Lynyrd Skynyrd", "difficulty": "easy"},
            {"title": "Take Me Home, Country Roads", "artist": "John Denver", "difficulty": "easy"},
            {"title": "Leaving on a Jet Plane", "artist": "John Denver", "difficulty": "easy"}
        ],
        'D': [
            {"title": "Knockin' on Heaven's Door", "artist": "Bob Dylan", "difficulty": "easy"},
            {"title": "Sweet Child O' Mine", "artist": "Guns N' Roses", "difficulty": "medium"},
            {"title": "Wish You Were Here", "artist": "Pink Floyd", "difficulty": "medium"}
        ],
        'A': [
            {"title": "Brown Eyed Girl", "artist": "Van Morrison", "difficulty": "easy"},
            {"title": "Free Fallin'", "artist": "Tom Petty", "difficulty": "easy"},
            {"title": "Sweet Home Alabama", "artist": "Lynyrd Skynyrd", "difficulty": "easy"}
        ],
        'E': [
            {"title": "Wild Thing", "artist": "The Troggs", "difficulty": "easy"},
            {"title": "Sweet Home Alabama", "artist": "Lynyrd Skynyrd", "difficulty": "easy"},
            {"title": "Seven Nation Army", "artist": "The White Stripes", "difficulty": "easy"}
        ],
        'Am': [
            {"title": "House of the Rising Sun", "artist": "The Animals", "difficulty": "medium"},
            {"title": "Sultans of Swing", "artist": "Dire Straits", "difficulty": "hard"},
            {"title": "Stairway to Heaven", "artist": "Led Zeppelin", "difficulty": "hard"}
        ],
        'Em': [
            {"title": "Nothing Else Matters", "artist": "Metallica", "difficulty": "medium"},
            {"title": "Zombie", "artist": "The Cranberries", "difficulty": "medium"},
            {"title": "Boulevard of Broken Dreams", "artist": "Green Day", "difficulty": "medium"}
        ],
        'F': [
            {"title": "Hallelujah", "artist": "Leonard Cohen", "difficulty": "medium"},
            {"title": "Ring of Fire", "artist": "Johnny Cash", "difficulty": "medium"},
            {"title": "Hey Jude", "artist": "The Beatles", "difficulty": "medium"}
        ],
        'Dm': [
            {"title": "Another Brick in the Wall", "artist": "Pink Floyd", "difficulty": "medium"},
            {"title": "Hit the Road Jack", "artist": "Ray Charles", "difficulty": "medium"},
            {"title": "Stairway to Heaven", "artist": "Led Zeppelin", "difficulty": "hard"}
        ],
        'G7': [
            {"title": "Sweet Home Chicago", "artist": "Robert Johnson", "difficulty": "medium"},
            {"title": "Twist and Shout", "artist": "The Beatles", "difficulty": "easy"},
            {"title": "Johnny B. Goode", "artist": "Chuck Berry", "difficulty": "medium"}
        ]
    }

    # 기본 코드 추출 (장식음 제거)
    basic_chord = chord_name
    if len(chord_name) > 1 and chord_name[1] in ['#', 'b']:
        basic_root = chord_name[:2]
        basic_chord = basic_root

        # 마이너 코드 확인
        if len(chord_name) > 2 and chord_name[2] == 'm' and chord_name[2:] != 'maj7':
            basic_chord = basic_root + 'm'
    else:
        basic_root = chord_name[:1]
        basic_chord = basic_root

        # 마이너 코드 확인
        if len(chord_name) > 1 and chord_name[1] == 'm' and chord_name[1:] != 'maj7':
            basic_chord = basic_root + 'm'

    # 해당 코드의 곡 목록 반환
    if basic_chord in chord_songs:
        return chord_songs[basic_chord]

    # 기본 코드에 대한 정보가 없으면 비슷한 코드 찾기
    similar_chords = []
    for chord in chord_songs.keys():
        # 동일한 루트 음을 가진 다른 코드
        if chord[0] == basic_chord[0]:
            similar_chords.append(chord)

    # 비슷한 코드의 곡 목록 반환 (있는 경우)
    if similar_chords and similar_chords[0] in chord_songs:
        result = chord_songs[similar_chords[0]]
        for song in result:
            song["note"] = f"이 곡은 {chord_name}가 아닌 {similar_chords[0]} 코드를 사용합니다."
        return result

    # 아무 정보도 없으면 빈 목록 반환
    return []