import csv
import json

def convert_structure_to_array(structure_string):
    """세미콜론으로 구분된 코드 구조 문자열을 배열로 변환"""
    if not structure_string:
        return []
    return structure_string.split(';')


def convert_chordfingers_to_json():
    # 구조화된 JSON 데이터 생성
    chord_data = {}

    with open('chord-fingers.csv', 'r') as f:
        reader = csv.DictReader(f, delimiter=';')
        for row in reader:
            root = row['CHORD_ROOT']
            chord_type = row['CHORD_TYPE']

            # 중첩 딕셔너리 구조 생성
            if root not in chord_data:
                chord_data[root] = {}

            if chord_type not in chord_data[root]:
                chord_data[root][chord_type] = []

            # 손가락 위치 데이터 처리
            finger_positions = row['FINGER_POSITIONS'].split(',')
            notes = row['NOTE_NAMES'].split(',')

            # 코드 구조를 배열로 변환
            structure = convert_structure_to_array(row['CHORD_STRUCTURE'])

            chord_data[root][chord_type].append({
                'fingers': finger_positions,
                'notes': notes,
                'structure': structure  # 배열로 저장
            })

    # JSON 파일로 저장
    with open('../models/audio/chord_database.json', 'w') as f:
        json.dump(chord_data, f, indent=2)

convert_chordfingers_to_json()