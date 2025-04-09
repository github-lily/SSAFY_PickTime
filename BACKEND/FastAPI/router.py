# router.py
from fastapi import APIRouter, UploadFile, File, Path, HTTPException
from session_manager import create_session, get_session, remove_session
import numpy as np
import cv2
import logging
from typing import List

logger = logging.getLogger(__name__)

api_router = APIRouter()

@api_router.get("/test")
def index():
    return "test 성공"

@api_router.post("/init")
def init_session():
    try:
        session_id = create_session()
        logger.info(f"세션 생성됨: {session_id}")
        return {"session_id": session_id}
    except Exception as e:
        logger.exception("세션 생성 오류")
        raise HTTPException(status_code=500, detail="세션 생성에 실패했습니다.")

# 검출용
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

# 추적용
@api_router.post("/tracking/{session_id}")
def tracking(
    session_id: str = Path(...),
    files: List[UploadFile] = File(...)  # 다수의 파일을 받도록 수정
):
    tracker = get_session(session_id)
    if tracker is None:
        raise HTTPException(status_code=400, detail="Invalid session_id")
    
    results = []  # 각 파일에 대한 추론 결과를 담을 리스트
    for file in files:
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
        print(result)
        results.append(result)
    
    # 여러 결과 중에서 overall detection_done은 하나라도 True면 True로 처리
    overall_detection = any(r.get("detection_done", False) for r in results)
    
    # 각 손가락(finger)의 결과를 집계하는 함수
    def aggregate_finger_positions(results):
        aggregate = {}
        # 각 결과마다 finger_positions를 순회
        for res in results:
            fps = res.get("finger_positions", {})
            for finger_id, pos in fps.items():
                if finger_id not in aggregate:
                    aggregate[finger_id] = {"fretboard": [], "string": []}
                fb = pos.get("fretboard")
                st = pos.get("string")
                if fb is not None:
                    aggregate[finger_id]["fretboard"].append(fb)
                if st is not None:
                    aggregate[finger_id]["string"].append(st)
        
        final = {}
        # 각 손가락에 대해 평균값과 가장 가까운 값을 선택
        for finger_id, lists in aggregate.items():
            final[finger_id] = {}
            for key in ["fretboard", "string"]:
                values = lists.get(key, [])
                if values:
                    avg = sum(values) / len(values)
                    # 평균과의 차이가 최소인 값을 선택
                    best_val = min(values, key=lambda x: abs(x - avg))
                    final[finger_id][key] = best_val
                else:
                    final[finger_id][key] = None
        return final

    aggregated_positions = aggregate_finger_positions(results)
    
    return {
        "detection_done": overall_detection,
        "finger_positions": aggregated_positions
    }

@api_router.post("/stop/{session_id}")
def stop_session(
    session_id: str = Path(...)
):
    if get_session(session_id) is None:
        raise HTTPException(status_code=400, detail="Invalid session_id")
    remove_session(session_id)
    logger.info(f"세션 삭제됨됨")
    return {"message": f"Session {session_id} stopped."}
