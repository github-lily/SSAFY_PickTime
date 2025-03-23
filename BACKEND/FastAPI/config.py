"""
Configuration settings for guitar chord assistant.
"""

import os
import cv2
from pathlib import Path

# Project root directory
PROJECT_ROOT = Path(__file__).parent.absolute()

# Directories
TEMP_DIR = os.path.join(PROJECT_ROOT, 'temp')
MODELS_DIR = os.path.join(PROJECT_ROOT, 'models')

# Create directories if they don't exist
os.makedirs(TEMP_DIR, exist_ok=True)
os.makedirs(os.path.join(MODELS_DIR, 'hand_detection'), exist_ok=True)
os.makedirs(os.path.join(MODELS_DIR, 'chord_classification'), exist_ok=True)

# Model paths
HAND_DETECTION_MODEL = os.path.join(MODELS_DIR, 'hand_detection', 'model_state_dict.zip')
CHORD_MOBILENET_MODEL = os.path.join(MODELS_DIR, 'chord_classification', 'mobilenet_model.pth')

# MediaPipe parameters
MP_STATIC_MODE = False
MP_MAX_HANDS = 2
MP_DETECTION_CONFIDENCE = 0.7
MP_TRACKING_CONFIDENCE = 0.5

# Image processing
IMAGE_RESIZE_WIDTH = 640
IMAGE_RESIZE_HEIGHT = 480

# Visualization
FINGERTIP_CIRCLE_RADIUS = 5
FINGERTIP_CIRCLE_COLOR = (0, 255, 0)  # BGR format
TEXT_COLOR = (255, 255, 255)  # White
STATUS_COLOR = (0, 255, 0)  # Green
TEXT_FONT = cv2.FONT_HERSHEY_SIMPLEX
TEXT_SCALE = 0.5
TEXT_THICKNESS = 1
STATUS_SCALE = 0.7
STATUS_THICKNESS = 2

# Fingertip indices in MediaPipe hand model
FINGERTIP_INDICES = {
    "thumb": 4,
    "index": 8,
    "middle": 12,
    "ring": 16,
    "pinky": 20
}

# 오디오 처리 관련 설정
AUDIO_CONFIG = {
    "sample_rate": 16000,
    "chunk_size": 1024,
    "analysis_window": 1.0,  # 초
    "confidence_threshold": 0.5,
    "model_size": "tiny",  # CREPE 모델 크기 (tiny, small, medium, large, full)
    "crepe_model_path": "models/audio/crepe_tflite/crepe_tiny.tflite",
    "chord_db_path": "models/audio/chord_database.json"
}

# 코드 감지와 손가락 위치 매핑을 위한 설정
CHORD_HAND_MAPPING = {
    "C": {
        "frets": [0, 1, 0, 2, 3, 0],  # 각 현의 프렛 위치 (1번 현부터 6번 현까지)
        "fingers": [0, 1, 0, 2, 3, 0]  # 각 프렛을 누르는 손가락 번호 (1:검지, 2:중지, 3:약지, 4:소지)
    },
    "G": {
        "frets": [3, 0, 0, 0, 2, 3],
        "fingers": [2, 0, 0, 0, 1, 3]
    },
    "D": {
        "frets": [2, 3, 2, 0, 0, 0],
        "fingers": [2, 3, 1, 0, 0, 0]
    },
    "A": {
        "frets": [0, 0, 2, 2, 2, 0],
        "fingers": [0, 0, 1, 2, 3, 0]
    },
    "E": {
        "frets": [0, 0, 1, 2, 2, 0],
        "fingers": [0, 0, 1, 3, 2, 0]
    },
    "Am": {
        "frets": [0, 0, 2, 2, 1, 0],
        "fingers": [0, 0, 2, 3, 1, 0]
    },
    "Em": {
        "frets": [0, 0, 0, 2, 2, 0],
        "fingers": [0, 0, 0, 2, 1, 0]
    },
    # 더 많은 코드 추가 가능
}
