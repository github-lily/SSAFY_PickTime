"""
FastAPI WebSocket integration for real-time guitar chord recognition.

This module provides WebSocket endpoints for real-time communication
between the frontend and the guitar chord recognition system.
"""

import cv2 as cv
import librosa
import numpy as np
import base64
import json
import asyncio
import logging
from typing import List
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
import time
from src.audio.audio_pipeline import GuitarAudioPipeline

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("guitar-chord-websocket")


# Connection manager for WebSocket clients
class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    def disconnect(self, websocket: WebSocket):
        self.active_connections.remove(websocket)

    async def send_text(self, message: str, websocket: WebSocket):
        await websocket.send_text(message)

    async def send_json(self, message: dict, websocket: WebSocket):
        await websocket.send_json(message)

    async def send_bytes(self, data: bytes, websocket: WebSocket):
        await websocket.send_bytes(data)


# Global variables
manager = ConnectionManager()
processing_lock = asyncio.Lock()
detection_model = None
frame_counter = 0
last_frame_time = 0
processing_fps = 0


def init_app(app: FastAPI):
    """
    Initialize WebSocket endpoints for the FastAPI application.

    Args:
        app: FastAPI application instance
    """

    @app.websocket("/ws/guitar-assistant")
    async def guitar_assistant_websocket(websocket: WebSocket):
        """
        WebSocket endpoint for real-time guitar chord recognition.

        The client sends frames as base64-encoded images and receives recognition results.
        """
        await manager.connect(websocket)

        try:
            global frame_counter, last_frame_time, processing_fps

            # Send initial connection confirmation
            await manager.send_json({
                "type": "connection_established",
                "message": "Connected to Guitar Chord Assistant"
            }, websocket)

            while True:
                # Wait for a message from the client
                message = await websocket.receive_text()

                # Try to parse the message as JSON
                try:
                    data = json.loads(message)
                    message_type = data.get("type", "")

                    # Handle different message types
                    if message_type == "frame":
                        # Process a video frame
                        frame_base64 = data.get("frame", "")

                        if not frame_base64:
                            await manager.send_json({
                                "type": "error",
                                "message": "No frame datasets provided"
                            }, websocket)
                            continue

                        # Process the frame (with lock to prevent parallel processing)
                        async with processing_lock:
                            try:
                                # Decode base64 to frame
                                frame_bytes = base64.b64decode(
                                    frame_base64.split(',')[1] if ',' in frame_base64 else frame_base64)
                                frame_arr = np.frombuffer(frame_bytes, np.uint8)
                                frame = cv.imdecode(frame_arr, cv.IMREAD_COLOR)

                                if frame is None:
                                    raise ValueError("Could not decode frame")

                                # Calculate FPS
                                current_time = time.time()
                                if frame_counter == 0:
                                    last_frame_time = current_time
                                frame_counter += 1

                                if current_time - last_frame_time > 1.0:
                                    processing_fps = frame_counter / (current_time - last_frame_time)
                                    frame_counter = 0
                                    last_frame_time = current_time

                                # Process the frame (this would call the guitar chord recognition)
                                result = await process_frame(frame, data.get("options", {}))

                                # Send the result back to the client
                                await manager.send_json(result, websocket)

                            except Exception as e:
                                logger.error(f"Error processing frame: {str(e)}")
                                await manager.send_json({
                                    "type": "error",
                                    "message": f"Error processing frame: {str(e)}"
                                }, websocket)

                    elif message_type == "set_learning_mode":
                        # Set learning mode and target chord
                        target_chord = data.get("chord", "")
                        difficulty = data.get("difficulty", "beginner")

                        # Configure learning mode
                        learning_config = {
                            "mode": "learning",
                            "target_chord": target_chord,
                            "difficulty": difficulty
                        }

                        await manager.send_json({
                            "type": "learning_mode_set",
                            "config": learning_config
                        }, websocket)

                    elif message_type == "ping":
                        # Simple ping to keep connection alive
                        await manager.send_json({
                            "type": "pong",
                            "timestamp": time.time()
                        }, websocket)

                    else:
                        # Unknown message type
                        await manager.send_json({
                            "type": "error",
                            "message": f"Unknown message type: {message_type}"
                        }, websocket)

                except json.JSONDecodeError:
                    await manager.send_json({
                        "type": "error",
                        "message": "Invalid JSON message"
                    }, websocket)
                except Exception as e:
                    logger.error(f"Error handling WebSocket message: {str(e)}")
                    await manager.send_json({
                        "type": "error",
                        "message": f"Error: {str(e)}"
                    }, websocket)

        except WebSocketDisconnect:
            manager.disconnect(websocket)
            logger.info("Client disconnected from WebSocket")
        except Exception as e:
            logger.error(f"WebSocket error: {str(e)}")
            manager.disconnect(websocket)

    # 오디오 데이터 스트리밍 및 처리
    @app.websocket("/ws/audio")
    async def audio_websocket(websocket: WebSocket):
        """오디오 데이터 처리를 위한 웹소켓 핸들러"""
        await websocket.accept()

        pipeline = GuitarAudioPipeline()

        try:
            while True:
                # 오디오 데이터 수신
                data = await websocket.receive_text()
                data = json.loads(data)

                if "audio" in data:
                    # base64 오디오 데이터 처리
                    audio_bytes = base64.b64decode(data["audio"])
                    audio_data = np.frombuffer(audio_bytes, dtype=np.float32)
                    sr = data.get("sampleRate", 16000)

                    # 기타 연주 분석
                    result = pipeline.analyze_audio_stream(
                        audio_data,
                        sr,
                        expected_chord=data.get("expectedChord")
                    )

                    # 분석 결과 전송
                    await websocket.send_json(result)

                    # 프레임 시각화 데이터를 요청한 경우
                    if data.get("visualize"):
                        # 스펙트로그램 계산
                        D = librosa.amplitude_to_db(
                            np.abs(librosa.stft(audio_data)),
                            ref=np.max
                        )

                        # 이미지로 변환하여 base64로 인코딩
                        from matplotlib import pyplot as plt
                        import io

                        plt.figure(figsize=(5, 3))
                        librosa.display.specshow(D, sr=sr, x_axis='time', y_axis='log')
                        plt.colorbar(format='%+2.0f dB')
                        plt.tight_layout()

                        buf = io.BytesIO()
                        plt.savefig(buf, format='png')
                        buf.seek(0)

                        # base64 인코딩
                        import base64
                        img_data = base64.b64encode(buf.read()).decode('utf-8')

                        # 결과 전송
                        await websocket.send_json({
                            "spectrogram": img_data
                        })

                        plt.close()

        except WebSocketDisconnect:
            print("클라이언트 연결 해제")
        except Exception as e:
            print(f"오류 발생: {e}")

    return app


async def process_frame(frame, options=None):
    """
    Process a frame for guitar chord recognition.

    This is an async wrapper around the synchronous processing functions,
    allowing the WebSocket server to remain responsive.

    Args:
        frame: Video frame to process
        options: Optional processing options

    Returns:
        Dictionary with processing results
    """
    # Default options
    if options is None:
        options = {}

    # Get dependencies from global modules
    from src.detection.guitar_detection import GuitarNeckDetector
    import src.detection.fretboard_detection as fretboard_detection
    import src.tracking.hand_tracking as hand_tracking
    import utils.chord_recognition as chord_recognition

    try:
        # This would be the actual processing logic
        # For heavy processing, we use run_in_executor to not block the event loop
        loop = asyncio.get_event_loop()

        # Use a wrapper function to call all the synchronous processing code
        def process_frame_sync():
            global detection_model

            # Initialize detection model if not already done
            if detection_model is None:
                model_path = options.get("model_path", "models/guitar_neck_detection/train/weights/best.pt")
                detection_model = GuitarNeckDetector(model_path=model_path)

            # Default result structure
            result = {
                "type": "frame_processed",
                "timestamp": time.time(),
                "success": False,
                "neck_detected": False,
                "fretboard_detected": False,
                "hand_detected": False,
                "chord_recognized": False,
                "fps": processing_fps,
                "visualization": None
            }

            # Detect guitar neck
            detections = detection_model.detect(frame)

            # Process neck region if detected
            if detections["crops"]:
                result["neck_detected"] = True
                neck_crop = detections["crops"][0]["image"]
                neck_position = detections["crops"][0]["position"]

                # Detect strings and frets
                lines = fretboard_detection.detect_strings_and_frets(
                    neck_crop,
                    string_threshold=200,
                    fret_threshold=150
                )

                # Create virtual fretboard
                if lines["strings"] is not None and lines["frets"] is not None:
                    fretboard = fretboard_detection.create_virtual_fretboard(
                        lines["strings"],
                        lines["frets"],
                        neck_crop.shape
                    )

                    if fretboard_detection.is_valid_fretboard(fretboard):
                        result["fretboard_detected"] = True
                        result["success"] = True

                        # Setup hand tracking and track hands
                        hands_model = hand_tracking.setup_hand_tracking()
                        hand_results = hand_tracking.track_hand(frame, hands_model)

                        if hand_results is not None:
                            result["hand_detected"] = True

                            # Map hand to fretboard
                            finger_mappings = hand_tracking.map_hand_to_fretboard(
                                hand_results, fretboard, frame.shape)

                            if finger_mappings:
                                # Recognize chord
                                chord_info = chord_recognition.recognize_chord(finger_mappings)

                                if chord_info["chord_name"] != "Unknown":
                                    result["chord_recognized"] = True
                                    result["chord_info"] = {
                                        "name": chord_info["chord_name"],
                                        "confidence": chord_info["confidence"]
                                    }

                                # Create visualization
                                vis_frame = frame.copy()
                                # Draw neck box
                                x1, y1, x2, y2 = neck_position
                                cv.rectangle(vis_frame, (x1, y1), (x2, y2), (0, 255, 0), 2)

                                # Visualize fretboard and fingers
                                vis_neck = hand_tracking.visualize_finger_mapping(
                                    neck_crop, fretboard, finger_mappings, chord_info)

                                # Insert visualization back into frame
                                vis_frame[y1:y2, x1:x2] = vis_neck

                                # Add FPS info
                                cv.putText(
                                    vis_frame,
                                    f"FPS: {processing_fps:.1f}",
                                    (10, 30),
                                    cv.FONT_HERSHEY_SIMPLEX,
                                    1.0,
                                    (0, 255, 0),
                                    2
                                )

                                # Convert to base64 for sending over WebSocket
                                _, buffer = cv.imencode('.jpg', vis_frame)
                                visualization_base64 = base64.b64encode(buffer).decode('utf-8')
                                result["visualization"] = f"datasets:image/jpeg;base64,{visualization_base64}"

            return result

        # Run the processing functions in a thread pool
        result = await loop.run_in_executor(None, process_frame_sync)
        return result

    except Exception as e:
        logger.error(f"Error in process_frame: {str(e)}")
        return {
            "type": "error",
            "message": f"Processing error: {str(e)}"
        }


# Example of how to use in a FastAPI application
def create_websocket_app():
    """
    Create a FastAPI application with WebSocket support.

    Returns:
        Configured FastAPI application instance
    """
    app = FastAPI(
        title="Guitar Chord Assistant WebSocket API",
        description="WebSocket API for real-time guitar chord recognition",
        version="1.0.0"
    )

    return init_app(app)


# Can be used to test the WebSocket server standalone
if __name__ == "__main__":
    import uvicorn

    app = create_websocket_app()


    # Add a simple index route for testing
    @app.get("/")
    async def get_index():
        return {
            "message": "Guitar Chord Assistant WebSocket API is running",
            "websocket_endpoint": "/ws/guitar-assistant"
        }


    # Run the server
    uvicorn.run(app, host="0.0.0.0", port=8000)