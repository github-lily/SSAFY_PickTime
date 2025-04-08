# router.py
from fastapi import APIRouter, UploadFile, File, Path, HTTPException
from session_manager import create_session, get_session, remove_session
import numpy as np
import cv2
import logging

logger = logging.getLogger(__name__)

api_router = APIRouter()

@api_router.get("/test")
def index():
    return "test 성공"

@api_router.post("/init")
def init_session():
    try:
        session_id = create_session()
        return {"session_id": session_id}
    except Exception as e:
        logger.exception("세션 생성 오류")
        raise HTTPException(status_code=500, detail="세션 생성에 실패했습니다.")

@api_router.post("/detect/{session_id}")
def detect(
    session_id: str = Path(...),
    file: UploadFile = File(...)
):
    tracker = get_session(session_id)
    if tracker is None:
        raise HTTPException(status_code=400, detail="Invalid session_id")
    try:
        file_bytes = file.file.read()
        np_arr = np.frombuffer(file_bytes, np.uint8)
        frame = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
        if frame is None:
            raise ValueError("Failed to decode image")
    except Exception as e:
        logger.exception("이미지 디코딩 오류")
        raise HTTPException(status_code=400, detail="Failed to decode image")
    result = tracker.process_frame(frame)
    return {
        "detection_done": result["detection_done"],
        "finger_positions": result["finger_positions"]
    }

@api_router.post("/stop")
def stop_session(session_id: str):
    if get_session(session_id) is None:
        raise HTTPException(status_code=400, detail="Invalid session_id")
    remove_session(session_id)
    return {"message": f"Session {session_id} stopped."}
