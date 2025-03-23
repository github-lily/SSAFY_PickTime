import os
import glob
import numpy as np


def convert_polygon_to_segment(input_dir, output_dir=None):
    """
    폴리곤 라벨을 세그멘테이션 형식으로 변환

    Args:
        input_dir: 입력 라벨 파일 디렉토리
        output_dir: 출력 라벨 파일 디렉토리 (None이면 원본 파일 수정)
    """
    if output_dir:
        os.makedirs(output_dir, exist_ok=True)

    label_files = glob.glob(os.path.join(input_dir, "*.txt"))
    for file_path in label_files:
        if output_dir:
            output_file = os.path.join(output_dir, os.path.basename(file_path))
        else:
            output_file = file_path + ".new"

        with open(file_path, 'r') as f_in, open(output_file, 'w') as f_out:
            for line in f_in:
                parts = line.strip().split()
                if len(parts) < 5:  # 너무 짧은 라인 건너뛰기
                    continue

                class_id = parts[0]
                # 좌표가 전부 추출되도록 확인
                coords = [float(x) for x in parts[1:]]

                # 세그멘테이션 형식으로 작성
                # 클래스 ID + 모든 좌표 쌍
                f_out.write(f"{class_id} " + " ".join(f"{x}" for x in coords) + "\n")

        # 변환이 완료되면 파일 교체 (원본 파일 수정 모드)
        if not output_dir:
            os.replace(output_file, file_path)
            print(f"Converted: {file_path}")
        else:
            print(f"Saved to: {output_file}")


# 변환 실행
train_labels_dir = r"C:\Users\SSAFY\Desktop\project\B101_guitar_AI\datasets\guitar_string_fret_dataset\train\labels"
valid_labels_dir = r"C:\Users\SSAFY\Desktop\project\B101_guitar_AI\datasets\guitar_string_fret_dataset\valid\labels"
test_labels_dir = r"C:\Users\SSAFY\Desktop\project\B101_guitar_AI\datasets\guitar_string_fret_dataset\test\labels"

# 각 디렉토리 변환
for dir_path in [train_labels_dir, valid_labels_dir, test_labels_dir]:
    if os.path.exists(dir_path):
        print(f"Converting files in {dir_path}...")
        convert_polygon_to_segment(dir_path)
    else:
        print(f"Directory not found: {dir_path}")