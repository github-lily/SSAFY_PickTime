# Guitar Chord Assistant

실시간 기타 코드 학습 도우미 시스템입니다. 기타의 넥을 감지하고, 현과 프렛을 추출하며, 손가락 위치를 추적하여 사용자가 올바른 기타 코드를 연주할 수 있도록 도와줍니다.

## 설치 방법

### 사전 요구사항

- Python 3.8 이상 (작업환경: 3.12)
- CUDA 지원 GPU (선택사항, 없어도 작동 가능)

### 설치

1. 저장소 복제
```bash
git clone https://github.com/your-username/guitar-chord-assistant.git
cd guitar-chord-assistant
```

2. 가상환경 생성 및 활성화 (선택사항)
```bash
python -m venv venv
```
```bash
# Windows
venv\Scripts\activate
```
```bash
# Mac/Linux
source venv/bin/activate
```

3. 필요한 패키지 설치
```bash
pip install -r requirements.txt
```

## 사용 순서

### 1. 데이터셋 준비

데이터셋을 다음과 같은 구조로 준비합니다:

```
data/
└── guitar_neck_dataset/
    ├── train/
    │   ├── images/      # 훈련용 이미지 파일들 (.jpg, .png 등)
    │   └── labels/      # 훈련용 라벨 파일들 (.txt)
    ├── valid/
    │   ├── images/      # 검증용 이미지 파일들
    │   └── labels/      # 검증용 라벨 파일들
    └── test/
        ├── images/      # 테스트용 이미지 파일들
        └── labels/      # 테스트용 라벨 파일들
```

각 이미지에 대한 라벨 파일(.txt)은 YOLO 형식으로 작성되어야 합니다:
```
클래스_인덱스 x_center y_center width height
```

- `클래스_인덱스`: 0 (기타 넥), 1 (너트) 등의 클래스 인덱스
- `x_center`, `y_center`: 바운딩 박스 중심의 정규화된 좌표 (0-1 사이 값)
- `width`, `height`: 바운딩 박스의 정규화된 너비와 높이 (0-1 사이 값)

### 2. 모델 학습

다음 명령어로 기타 넥 감지 모델을 학습합니다:

```bash
python main.py train --dataset ./datasets/guitar_neck_dataset --output models
```

추가 옵션:
- `--epochs`: 학습 에포크 수 (기본값: 100)
- `--batch-size`: 배치 크기 (기본값: 16)

학습된 모델은 `models/guitar_neck_detection/train/weights/` 디렉토리에 저장됩니다.

### 3. 웹캠 데모 실행

학습된 모델을 사용하여 웹캠 데모를 실행합니다:

```bash
python main.py webcam --model models/guitar_neck_detection/train/weights/best.pt
```

추가 옵션:
- `--camera`: 카메라 ID (기본값: 0)
- `--learning`: 학습 모드 활성화 (타겟 코드 제공)
- `--debug`: 디버그 이미지 저장
- `--cpu`: GPU 사용 안함 (CPU 강제 사용)

### 4. API 서버 실행

Spring 백엔드와 통합을 위한 REST API 서버 실행:

```bash
python main.py api --model models/guitar_neck_detection/train/weights/best.pt
```

추가 옵션:
- `--host`: 서버 호스트 (기본값: "0.0.0.0")
- `--port`: 서버 포트 (기본값: 8000)
- `--cpu`: GPU 사용 안함 (CPU 강제 사용)

### 5. WebSocket 서버 실행

실시간 처리를 위한 WebSocket 서버 실행:

```bash
python main.py websocket --model models/guitar_neck_detection/train/weights/best.pt
```

추가 옵션:
- `--host`: 서버 호스트 (기본값: "0.0.0.0")
- `--port`: 서버 포트 (기본값: 8001)
- `--cpu`: GPU 사용 안함 (CPU 강제 사용)

## API 사용법

### REST API

기본 엔드포인트: `http://localhost:8000`

- `GET /health`: 서버 상태 확인
- `POST /analyze/image`: 이미지 분석 (multipart/form-data, form field: `file`)
- `POST /analyze/chord`: 코드 인식 (multipart/form-data, form field: `file`)

### WebSocket API

기본 엔드포인트: `ws://localhost:8001/ws/guitar-assistant`

- 프레임 전송: `{"type": "frame", "frame": "base64_encoded_image"}`
- 학습 모드 설정: `{"type": "set_learning_mode", "chord": "Am", "difficulty": "beginner"}`
- 응답 형식: `{"type": "frame_processed", "success": true, "chord_info": {...}, "visualization": "base64_encoded_image"}`

## 시스템 작동 방식

1. YOLOv12 모델로 기타 넥 영역 감지
2. 감지된 넥 영역에서 호프 변환을 사용하여 현과 프렛 추출
3. 가상 프렛보드 생성 및 최적화
4. MediaPipe를 사용하여 손 랜드마크 감지
5. 손가락 위치를 프렛보드에 매핑
6. 매핑된 손가락 위치를 기반으로 코드 인식
7. 결과 시각화 및 학습 피드백 제공

## 주요 기능

- 기타 넥 및 프렛보드 감지
- 현과 프렛 자동 추출
- MediaPipe를 활용한 손 추적
- 프렛보드 상의 손가락 위치 매핑
- 코드 인식 및 피드백
- 학습 모드 (타겟 코드 연습)
- REST API 및 WebSocket 인터페이스 제공

## 문제 해결

### 모델 학습 문제

- 충분한 데이터셋 확보 여부 확인
- CUDA 설치 확인 (GPU 학습 시)
- 배치 크기 축소 (메모리 부족 시)

### 실행 시 문제

- 카메라 연결 확인
- 모델 경로 정확성 확인
- CPU 모드 사용 (`--cpu` 옵션)
- 오류 로그 확인

## 라이센스

[MIT License](LICENSE)