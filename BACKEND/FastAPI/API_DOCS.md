# Guitar Chord Assistant API 문서

기타 코드 인식을 위한 REST API 및 WebSocket 서비스 문서입니다.

## 기본 정보

- Base URL: `{baseURL}/api/ai`
- Swagger UI: `{baseURL}/api/ai/docs`
- ReDoc: `{baseURL}/api/ai/redoc`

## 상태 확인 엔드포인트

### 루트 엔드포인트
- **URL**: `/api/ai/`
- **Method**: `GET`
- **Description**: API 상태 정보 제공
- **Response**: 
  ```json
  {
    "status": "online",
    "message": "Guitar Chord Assistant API is running",
    "models_loaded": true,
    "audio_pipeline_loaded": true,
    "version": "1.0.0"
  }
  ```

### 헬스 체크
- **URL**: `/api/ai/health`
- **Method**: `GET`
- **Description**: API 상태 확인용 헬스체크 엔드포인트
- **Response**: 
  ```json
  {
    "status": "healthy", 
    "models_loaded": true
  }
  ```

## 이미지 분석 엔드포인트

### 기타 이미지 분석
- **URL**: `/api/ai/image/analyze`
- **Method**: `POST`
- **Description**: 기타 이미지를 분석하여 기타 넥과 프렛보드 감지
- **Request**:
  - `file`: 이미지 파일 (Form 데이터)
  - `visualize`: 시각화 결과 포함 여부 (Boolean, 기본값: true)
- **Response**: 
  ```json
  {
    "success": true,
    "message": "Guitar neck processed successfully",
    "timestamp": 1679574321.45,
    "fretboard_detected": true,
    "hand_detected": false,
    "chord_recognized": false,
    "visualization_base64": "data:image/jpeg;base64,..."
  }
  ```

### 코드 인식
- **URL**: `/api/ai/chord/analyze`
- **Method**: `POST`
- **Description**: 기타 이미지에서 연주 중인 코드 인식
- **Request**:
  - `file`: 이미지 파일 (Form 데이터)
- **Response**: 
  ```json
  {
    "chord_name": "G Major",
    "confidence": 0.92,
    "positions": [
      {"string": 6, "fret": 3, "finger": 3},
      {"string": 5, "fret": 2, "finger": 2},
      {"string": 4, "fret": 0, "finger": null},
      {"string": 3, "fret": 0, "finger": null},
      {"string": 2, "fret": 0, "finger": null},
      {"string": 1, "fret": 3, "finger": 4}
    ]
  }
  ```

## 오디오 분석 엔드포인트

### 오디오 분석
- **URL**: `/api/ai/audio/analyze`
- **Method**: `POST`
- **Description**: 오디오 파일 분석하여 기타 코드 인식
- **Request**:
  - `file`: 오디오 파일 (Form 데이터)
  - `expected_chord`: 예상 코드 (Form 데이터, 선택사항)
- **Response**: 
  ```json
  {
    "detected_chord": "G",
    "chord_similarity": 0.85,
    "detected_notes": ["G", "B", "D"],
    "confidence": 0.92,
    "processing_time": 0.234
  }
  ```

### 코드 검증
- **URL**: `/api/ai/audio/verify-chord`
- **Method**: `POST`
- **Description**: 특정 코드가 올바르게 연주되었는지 검증
- **Request**:
  - `file`: 오디오 파일 (Form 데이터)
  - `chord`: 검증할 코드 이름 (Form 데이터)
- **Response**: 
  ```json
  {
    "expected_chord": "Am",
    "detected_chord": "Am",
    "is_correct": true,
    "accuracy": 0.94,
    "missing_notes": [],
    "extra_notes": [],
    "detected_notes": ["A", "C", "E"]
  }
  ```

### 코드 데이터베이스
- **URL**: `/api/ai/audio/chords`
- **Method**: `GET`
- **Description**: 코드 데이터베이스 정보 제공
- **Response**: 코드 정보 목록

## WebSocket 엔드포인트

### 실시간 기타 인식
- **URL**: `/api/ai/ws/guitar-assistant`
- **Description**: 실시간 기타 코드 인식을 위한 WebSocket 연결
- **메시지 형식**:
  - 클라이언트 → 서버:
    ```json
    {
      "type": "frame",
      "frame": "base64-encoded-image",
      "options": {
        "visualize": true
      }
    }
    ```
  - 서버 → 클라이언트:
    ```json
    {
      "type": "frame_processed",
      "timestamp": 1679574321.45,
      "success": true,
      "neck_detected": true,
      "fretboard_detected": true,
      "hand_detected": false,
      "chord_recognized": false,
      "fps": 15.6,
      "visualization": "data:image/jpeg;base64,..."
    }
    ```

### 실시간 오디오 분석
- **URL**: `/api/ai/ws/audio`
- **Description**: 실시간 오디오 분석을 위한 WebSocket 연결
- **메시지 형식**:
  - 클라이언트 → 서버:
    ```json
    {
      "audio": "base64-encoded-audio-data",
      "sampleRate": 16000,
      "expectedChord": "Am",
      "visualize": true
    }
    ```
  - 서버 → 클라이언트:
    ```json
    {
      "detected_chord": "Am",
      "chord_similarity": 0.92,
      "detected_notes": ["A", "C", "E"],
      "spectrogram": "data:image/png;base64,..."
    }
    ```

## 오류 처리

API는 다음과 같은 HTTP 상태 코드를 반환할 수 있습니다:

- **200 OK**: 요청이 성공적으로 처리됨
- **400 Bad Request**: 잘못된 요청 (예: 유효하지 않은 파일, 필수 매개변수 누락)
- **404 Not Found**: 리소스를 찾을 수 없음 (예: 기타 넥이나 손이 감지되지 않음)
- **500 Internal Server Error**: 서버 내부 오류
- **503 Service Unavailable**: 모델이 초기화되지 않음

## 클라이언트 예제

### Python 클라이언트 예제 (이미지 분석)

```python
import requests
import base64
from PIL import Image
import io

# 이미지 분석 요청
def analyze_image(image_path):
    url = "http://localhost:8000/api/ai/image/analyze"
    
    with open(image_path, "rb") as f:
        files = {"file": f}
        data = {"visualize": "true"}
        response = requests.post(url, files=files, data=data)
    
    if response.status_code == 200:
        result = response.json()
        
        # 시각화 이미지 저장 (있는 경우)
        if "visualization_base64" in result:
            # Base64 데이터 추출
            base64_data = result["visualization_base64"].split(",")[1]
            img_data = base64.b64decode(base64_data)
            
            # 이미지로 변환하여 저장
            img = Image.open(io.BytesIO(img_data))
            img.save("result.jpg")
            
        return result
    else:
        return {"error": f"Error: {response.status_code}", "message": response.text}

# 함수 호출
result = analyze_image("guitar.jpg")
print(result)
```

### JavaScript 클라이언트 예제 (WebSocket)

```javascript
// 오디오 WebSocket 연결 예제
function connectAudioWebSocket() {
    const ws = new WebSocket("ws://localhost:8000/api/ai/ws/audio");
    
    ws.onopen = () => {
        console.log("WebSocket connection established");
    };
    
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("Received chord analysis:", data);
        
        // 감지된 코드 표시
        if (data.detected_chord) {
            document.getElementById("chord-display").textContent = data.detected_chord;
        }
        
        // 스펙트로그램 표시 (있는 경우)
        if (data.spectrogram) {
            document.getElementById("spectrogram").src = data.spectrogram;
        }
    };
    
    ws.onclose = () => {
        console.log("WebSocket connection closed");
    };
    
    // 오디오 데이터 전송 함수
    return function sendAudioData(audioData, sampleRate) {
        if (ws.readyState === WebSocket.OPEN) {
            const message = {
                audio: btoa(String.fromCharCode.apply(null, new Uint8Array(audioData))),
                sampleRate: sampleRate,
                visualize: true
            };
            
            ws.send(JSON.stringify(message));
        }
    };
}

// 사용 예시
const sendAudio = connectAudioWebSocket();

// 오디오 데이터가 있을 때 함수 호출
audioRecorder.onDataAvailable = (audioBuffer) => {
    sendAudio(audioBuffer, 16000);
};
```

## 제한사항 및 주의사항

- 이미지는 JPEG 또는 PNG 형식이어야 합니다.
- 오디오는 WAV 형식이 권장됩니다.
- 이미지 및 오디오 파일 크기 제한: 10MB
- WebSocket 연결은 비활성 상태가 1분 지속되면 자동으로 종료됩니다.
- 모델 파일이 없으면 일부 기능이 제한될 수 있습니다.