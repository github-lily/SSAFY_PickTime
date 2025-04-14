# 모델 경로 (원본 코드에서 하드코딩된 경로를 옮김)
MODEL_PATH = "models/yolov8n_seg_fret_nut.pt"

NUM_FRETS = 20
CLASS_FRET = 0
CLASS_NUT = 1
MIN_SCORE_FRET = 0.5
MIN_SCORE_NUT = 0.5
STABLE_FRAMES = 5          # 안정 프레임 수
REDETECT_ERROR_THRESHOLD = 1
MAX_MISSING_FRAMES = 30