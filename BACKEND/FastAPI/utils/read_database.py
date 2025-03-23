import json


def extract_chord_types(json_file_path):
    # JSON 파일 로드
    with open(json_file_path, 'r') as f:
        chord_db = json.load(f)

    # 결과 저장 변수
    roots = []
    chord_types = set()

    # 모든 루트 음 추출
    roots = list(chord_db.keys())

    # 모든 코드 유형 추출
    for root, types in chord_db.items():
        for chord_type in types.keys():
            chord_types.add(chord_type)

    return {
        "roots": sorted(roots),
        "chord_types": sorted(list(chord_types)),
        "total_roots": len(roots),
        "total_chord_types": len(chord_types)
    }

# 사용 예시
result = extract_chord_types("../models/audio/chord_database.json")
print(f"루트 음: {result['roots']}")
print(f"코드 유형: {result['chord_types']}")
print(f"총 루트 음 개수: {result['total_roots']}")
print(f"총 코드 유형 개수: {result['total_chord_types']}")