"""
Guitar Chord Assistant API.

This module provides a FastAPI server to expose guitar chord assistant
functionality as a REST API and WebSocket service.
"""

import os
import cv2 as cv
import numpy as np
import base64
import time
import logging
import io
import librosa
import json
import asyncio
from fastapi import FastAPI, APIRouter, File, UploadFile, Form, HTTPException, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
from pydantic import BaseModel
from typing import List, Dict, Any, Optional

# Import project modules
import config
from src.detection.guitar_detection import GuitarNeckDetector
import src.detection.fretboard_detection as fretboard_detection
import src.tracking.hand_tracking as hand_tracking
from src.audio.audio_pipeline import GuitarAudioPipeline
from utils.chord_recognition import recognize_chord

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("guitar-chord-assistant-api")

# Initialize FastAPI app
app = FastAPI(
    title="Guitar Chord Assistant API",
    description="API for guitar chord recognition and learning assistance",
    version="1.0.0",
    docs_url="/api/ai/docs",  # Set Swagger UI path
    redoc_url="/api/ai/redoc",  # Set ReDoc path
    openapi_url="/api/ai/openapi.json",  # Set OpenAPI schema path
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Modify in production to specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Create main router with prefix
ai_router = APIRouter(prefix="/ai")

# Global detector instances
guitar_detector = None
audio_pipeline = None
processing_fps = 0

# Create sub-routers
image_router = APIRouter(prefix="/image", tags=["image"])
chord_router = APIRouter(prefix="/chord", tags=["chord"])
audio_router = APIRouter(prefix="/audio", tags=["audio"])
ws_router = APIRouter(prefix="/ws", tags=["websocket"])


# Pydantic models
class ChordPosition(BaseModel):
    string: int
    fret: int
    finger: Optional[int] = None


class ChordRecognitionResult(BaseModel):
    chord_name: str
    confidence: float
    positions: List[ChordPosition]


class DetectionResult(BaseModel):
    success: bool
    message: str
    timestamp: float
    frame_id: Optional[int] = None
    fretboard_detected: bool = False
    hand_detected: bool = False
    chord_recognized: bool = False
    chord_info: Optional[Dict[str, Any]] = None
    visualization_base64: Optional[str] = None


# Initialize models
@app.on_event("startup")
async def startup_event():
    global guitar_detector, audio_pipeline

    logger.info("Initializing Guitar Chord Assistant API...")

    # Create directories if they don't exist
    os.makedirs(config.TEMP_DIR, exist_ok=True)
    os.makedirs(os.path.join(config.MODELS_DIR, 'guitar_neck_detection', 'train', 'weights'), exist_ok=True)

    # Choose device
    device = 'cuda' if cv.cuda.getCudaEnabledDeviceCount() > 0 else 'cpu'
    logger.info(f"Using device: {device}")

    # Check if model exists
    model_path = os.path.join(config.MODELS_DIR, 'guitar_neck_detection', 'train', 'weights', 'best.pt')
    if not os.path.exists(model_path):
        logger.error(f"Guitar detection model not found at {model_path}")
        logger.info("Models need to be trained first using the training pipeline")
    else:
        # Initialize guitar detector
        try:
            logger.info(f"Loading guitar detection model from {model_path}")
            guitar_detector = GuitarNeckDetector(
                model_path=model_path,
                conf_threshold=0.5,
                device=device
            )
            logger.info("Guitar detector initialized successfully")
        except Exception as e:
            logger.error(f"Error initializing guitar detector: {str(e)}")

    # Initialize audio pipeline
    try:
        logger.info("Initializing audio pipeline")
        chord_db_path = os.path.join(config.MODELS_DIR, 'audio', 'chord_database.json')
        audio_pipeline = GuitarAudioPipeline(
            model_capacity=config.AUDIO_CONFIG["model_size"],
            chord_db_path=chord_db_path if os.path.exists(chord_db_path) else None
        )
        logger.info("Audio pipeline initialized successfully")
    except Exception as e:
        logger.error(f"Error initializing audio pipeline: {str(e)}")

    logger.info("API startup complete")


# Root endpoints
@ai_router.get("/")
async def root():
    """API root endpoint with status information."""
    return {
        "status": "online",
        "message": "Guitar Chord Assistant API is running",
        "models_loaded": guitar_detector is not None,
        "audio_pipeline_loaded": audio_pipeline is not None,
        "version": "1.0.0",
    }


@ai_router.get("/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy", "models_loaded": guitar_detector is not None}


# Image analysis endpoints
@image_router.post("/analyze", response_model=DetectionResult)
async def analyze_image(
        file: UploadFile = File(...),
        visualize: bool = Form(True)
):
    """
    Analyze a guitar image for neck and fretboard detection.

    Args:
        file: Image file to analyze
        visualize: Whether to return visualization

    Returns:
        Detection result with fretboard information
    """
    global guitar_detector, processing_fps

    # Check if models are loaded
    if guitar_detector is None:
        raise HTTPException(status_code=503, detail="Models not initialized")

    try:
        # Read the uploaded image
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        image = cv.imdecode(nparr, cv.IMREAD_COLOR)

        if image is None:
            raise HTTPException(status_code=400, detail="Invalid image file")

        # Record processing time
        start_time = time.time()

        # Process the image
        result = process_frame(image, guitar_detector, fretboard_detection, visualize=visualize)

        # Calculate FPS
        process_time = time.time() - start_time
        current_fps = 1.0 / process_time if process_time > 0 else 0
        processing_fps = 0.9 * processing_fps + 0.1 * current_fps  # Smoothed FPS

        # Prepare response
        response = {
            "success": result["success"],
            "message": "Guitar neck processed successfully" if result["success"] else "No valid guitar neck detected",
            "timestamp": time.time(),
            "fretboard_detected": result["success"],
            "hand_detected": False,  # Will be implemented later with hand tracking
            "chord_recognized": False,  # Will be implemented later with chord recognition
        }

        # Add visualization if requested
        if visualize and result["visualization"] is not None:
            # Add FPS information to visualization
            fps_text = f"Processing: {processing_fps:.1f} FPS"
            cv.putText(
                result["visualization"],
                fps_text,
                (10, 30),
                cv.FONT_HERSHEY_SIMPLEX,
                1,
                (0, 255, 0),
                2
            )

            # Encode the visualization as base64
            _, buffer = cv.imencode('.jpg', result["visualization"])
            visualization_base64 = base64.b64encode(buffer).decode('utf-8')
            response["visualization_base64"] = visualization_base64

        return response

    except Exception as e:
        logger.error(f"Error processing image: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error processing image: {str(e)}")


# Chord analysis endpoint
@chord_router.post("/analyze", response_model=ChordRecognitionResult)
async def analyze_chord(
        file: UploadFile = File(...),
):
    """
    Analyze a guitar image to recognize the chord being played.

    Args:
        file: Image file to analyze

    Returns:
        Recognized chord information
    """
    global guitar_detector

    # Check if models are loaded
    if guitar_detector is None:
        raise HTTPException(status_code=503, detail="Models not initialized")

    try:
        # Read the uploaded image
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        image = cv.imdecode(nparr, cv.IMREAD_COLOR)

        if image is None:
            raise HTTPException(status_code=400, detail="Invalid image file")

        # Set up hand tracking model
        hands_model = hand_tracking.setup_hand_tracking()

        # Detect guitar neck and fretboard
        result = process_frame(image, guitar_detector, fretboard_detection)

        if not result["success"]:
            raise HTTPException(status_code=404, detail="No valid guitar neck detected")

        # Detect hand
        hand_results = hand_tracking.track_hand(image, hands_model)

        if hand_results is None:
            raise HTTPException(status_code=404, detail="No hand detected on fretboard")

        # Map hand to fretboard
        finger_mappings = hand_tracking.map_hand_to_fretboard(
            hand_results, result["fretboard"], image.shape
        )

        if not finger_mappings:
            raise HTTPException(status_code=404, detail="Could not map fingers to fretboard")

        # Recognize chord
        chord_info = recognize_chord(finger_mappings)

        if chord_info["chord_name"] == "Unknown":
            raise HTTPException(status_code=404, detail="Could not recognize chord")

        # Convert finger mappings to positions format
        positions = []
        for finger, data in finger_mappings.items():
            position = ChordPosition(
                string=data.get("string", 0),
                fret=data.get("fret", 0),
                finger=int(finger.split("_")[0]) if "_" in finger else None
            )
            positions.append(position)

        return ChordRecognitionResult(
            chord_name=chord_info["chord_name"],
            confidence=chord_info["confidence"],
            positions=positions
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error analyzing chord: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error analyzing chord: {str(e)}")


# Audio analysis endpoints
@audio_router.post("/analyze")
async def analyze_audio(file: UploadFile = File(...), expected_chord: str = Form(None)):
    """
    Analyze an uploaded audio file to recognize the chord being played.

    Args:
        file: Uploaded audio file
        expected_chord: Expected chord (optional, for verification)

    Returns:
        Analysis result in JSON format
    """
    global audio_pipeline

    # Check if audio pipeline is loaded
    if audio_pipeline is None:
        raise HTTPException(status_code=503, detail="Audio pipeline not initialized")

    start_time = time.time()

    try:
        # Read file content
        contents = await file.read()

        # Load audio
        audio, sr = librosa.load(io.BytesIO(contents), sr=None)

        # Analyze audio
        if expected_chord:
            result = audio_pipeline.analyze_audio_stream(audio, sr, expected_chord)
        else:
            result = audio_pipeline.analyze_audio(audio, sr)

        # Add processing time
        result["processing_time"] = time.time() - start_time

        return JSONResponse(content=result)

    except Exception as e:
        logger.error(f"Error analyzing audio: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error analyzing audio: {str(e)}")


@audio_router.post("/verify-chord")
async def verify_chord(file: UploadFile = File(...), chord: str = Form(None)):
    """
    Verify if a specific chord is played correctly in the uploaded audio.

    Args:
        file: Uploaded audio file
        chord: Chord name to verify

    Returns:
        Verification result
    """
    global audio_pipeline

    if not chord:
        return JSONResponse(content={"error": "Please specify a chord to verify"}, status_code=400)

    # Check if audio pipeline is loaded
    if audio_pipeline is None:
        raise HTTPException(status_code=503, detail="Audio pipeline not initialized")

    try:
        # Read file content
        contents = await file.read()

        # Load audio
        audio, sr = librosa.load(io.BytesIO(contents), sr=None)

        # Analyze audio with expected chord
        result = audio_pipeline.analyze_audio_stream(audio, sr, expected_chord=chord)

        return JSONResponse(content=result)

    except Exception as e:
        logger.error(f"Error verifying chord: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error verifying chord: {str(e)}")


@audio_router.get("/chords")
async def get_chord_database():
    """
    Get chord database information.

    Returns:
        List of chord information
    """
    global audio_pipeline

    # Check if audio pipeline is loaded
    if audio_pipeline is None:
        raise HTTPException(status_code=503, detail="Audio pipeline not initialized")

    return JSONResponse(content=audio_pipeline.chord_recognizer.chord_db)


# WebSocket endpoints
# WebSocket connection manager
class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)
        logger.info(f"WebSocket client connected. Total connections: {len(self.active_connections)}")

    def disconnect(self, websocket: WebSocket):
        self.active_connections.remove(websocket)
        logger.info(f"WebSocket client disconnected. Remaining connections: {len(self.active_connections)}")

    async def send_json(self, message: dict, websocket: WebSocket):
        await websocket.send_json(message)


# Create connection manager instance
manager = ConnectionManager()


@ws_router.websocket("/guitar-assistant")
async def guitar_assistant_websocket(websocket: WebSocket):
    """WebSocket endpoint for real-time guitar chord recognition."""
    await manager.connect(websocket)

    try:
        # Send connection confirmation
        await manager.send_json({
            "type": "connection_established",
            "message": "Connected to Guitar Chord Assistant"
        }, websocket)

        while True:
            # Wait for message from client
            message = await websocket.receive_text()

            try:
                data = json.loads(message)
                message_type = data.get("type", "")

                # Handle different message types
                if message_type == "frame":
                    # Process video frame
                    frame_base64 = data.get("frame", "")

                    if not frame_base64:
                        await manager.send_json({
                            "type": "error",
                            "message": "No frame data provided"
                        }, websocket)
                        continue

                    try:
                        # Decode base64 to frame
                        frame_bytes = base64.b64decode(
                            frame_base64.split(',')[1] if ',' in frame_base64 else frame_base64)
                        frame_arr = np.frombuffer(frame_bytes, np.uint8)
                        frame = cv.imdecode(frame_arr, cv.IMREAD_COLOR)

                        if frame is None:
                            raise ValueError("Could not decode frame")

                        # Process the frame
                        result = process_frame(frame, guitar_detector, fretboard_detection)

                        # Create response
                        response = {
                            "type": "frame_processed",
                            "timestamp": time.time(),
                            "success": result["success"],
                            "neck_detected": result["success"],
                            "fretboard_detected": result["success"],
                            "hand_detected": False,
                            "chord_recognized": False,
                            "fps": processing_fps
                        }

                        # Add visualization if available
                        if result["visualization"] is not None:
                            _, buffer = cv.imencode('.jpg', result["visualization"])
                            visualization_base64 = base64.b64encode(buffer).decode('utf-8')
                            response["visualization"] = f"data:image/jpeg;base64,{visualization_base64}"

                        await manager.send_json(response, websocket)

                    except Exception as e:
                        logger.error(f"Error processing frame: {str(e)}")
                        await manager.send_json({
                            "type": "error",
                            "message": f"Error processing frame: {str(e)}"
                        }, websocket)

                elif message_type == "ping":
                    # Simple ping response
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

    except WebSocketDisconnect:
        manager.disconnect(websocket)
        logger.info("Client disconnected from WebSocket")


@ws_router.websocket("/audio")
async def audio_websocket(websocket: WebSocket):
    """WebSocket endpoint for real-time audio analysis."""
    global audio_pipeline

    await websocket.accept()

    try:
        # Check if audio pipeline is loaded
        if audio_pipeline is None:
            await websocket.send_json({
                "error": "Audio pipeline not initialized",
                "status": "error"
            })
            return

        while True:
            # Receive audio data
            data = await websocket.receive_text()
            data = json.loads(data)

            if "audio" in data:
                # Process base64 audio data
                audio_bytes = base64.b64decode(data["audio"])
                audio_data = np.frombuffer(audio_bytes, dtype=np.float32)
                sr = data.get("sampleRate", config.AUDIO_CONFIG["sample_rate"])

                # Analyze guitar playing
                result = audio_pipeline.analyze_audio_stream(
                    audio_data,
                    sr,
                    expected_chord=data.get("expectedChord")
                )

                # Send analysis result
                await websocket.send_json(result)

                # If visualization requested
                if data.get("visualize"):
                    try:
                        # Calculate spectrogram
                        D = librosa.amplitude_to_db(
                            np.abs(librosa.stft(audio_data)),
                            ref=np.max
                        )

                        # Convert to image and encode as base64
                        import matplotlib.pyplot as plt

                        plt.figure(figsize=(5, 3))
                        librosa.display.specshow(D, sr=sr, x_axis='time', y_axis='log')
                        plt.colorbar(format='%+2.0f dB')
                        plt.tight_layout()

                        buf = io.BytesIO()
                        plt.savefig(buf, format='png')
                        buf.seek(0)

                        img_data = base64.b64encode(buf.read()).decode('utf-8')

                        await websocket.send_json({
                            "spectrogram": f"data:image/png;base64,{img_data}"
                        })

                        plt.close()
                    except Exception as e:
                        logger.error(f"Error generating spectrogram: {str(e)}")
                        await websocket.send_json({
                            "error": f"Error generating spectrogram: {str(e)}"
                        })

    except WebSocketDisconnect:
        logger.info("Client disconnected from audio WebSocket")
    except Exception as e:
        logger.error(f"Error in audio WebSocket: {str(e)}")


@ai_router.get("/test")
async def test_endpoint():
    """테스트용 API 엔드포인트."""
    return {"message": "test 완료"}


# Helper functions
def process_frame(frame, detector, fretboard_detector, visualize=True):
    """
    Process a video frame to detect guitar necks and create virtual fretboard.

    Args:
        frame: Video frame to process
        detector: GuitarNeckDetector instance
        fretboard_detector: Module for fretboard detection
        visualize: If True, create visualization

    Returns:
        Dictionary with processing results and visualization
    """
    # Detect guitar necks
    detections = detector.detect(frame)

    # Initialize result
    result = {
        'success': False,
        'fretboard': None,
        'visualization': None
    }

    # Process neck crops
    if detections['crops']:
        # Take the first neck crop
        neck_crop = detections['crops'][0]['image']
        neck_position = detections['crops'][0]['position']

        # Detect strings and frets
        lines = fretboard_detector.detect_strings_and_frets(
            neck_crop,
            string_threshold=200,
            fret_threshold=150,
            verbose=False
        )

        # Create virtual fretboard
        if lines['strings'] is not None and lines['frets'] is not None:
            # Analyze neck orientation
            string_angle, fret_angle = fretboard_detector.analyze_neck_orientation(
                neck_crop, neck_position
            )

            fretboard = fretboard_detector.create_virtual_fretboard(
                lines['strings'],
                lines['frets'],
                neck_crop.shape,
                string_angle=string_angle,
                fret_angle=fret_angle
            )

            if fretboard_detector.is_valid_fretboard(fretboard):
                result['success'] = True
                result['fretboard'] = fretboard
                result['neck_position'] = neck_position

                # Create visualization if requested
                if visualize:
                    # Visualize neck detection
                    vis_frame = detector.visualize_detections(frame, detections)

                    # Visualize fretboard in the neck crop
                    vis_neck = fretboard_detector.visualize_fretboard(neck_crop, fretboard)

                    # Insert the visualized neck back into the frame
                    x1, y1, x2, y2 = neck_position
                    vis_frame[y1:y2, x1:x2] = vis_neck

                    result['visualization'] = vis_frame

    # If no visualization was created but it was requested
    if visualize and result['visualization'] is None:
        result['visualization'] = detector.visualize_detections(frame, detections)

    return result


# Include all routers
ai_router.include_router(image_router)
ai_router.include_router(chord_router)
ai_router.include_router(audio_router)
ai_router.include_router(ws_router)

# Include main router in app
app.include_router(ai_router)

# Run the application
if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Guitar Chord Assistant API Server')
    parser.add_argument('--host', type=str, default='0.0.0.0', help='Host to run the API server on')
    parser.add_argument('--port', type=int, default=8000, help='Port to run the API server on')
    parser.add_argument('--model', type=str, default=None, help='Path to guitar detection model')

    args = parser.parse_args()

    # Update model path if provided
    if args.model:
        config.GUITAR_DETECTION_MODEL = args.model

    # Run the API server
    uvicorn.run("app:app", host=args.host, port=args.port, reload=True)
