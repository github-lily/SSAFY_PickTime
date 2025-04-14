# schemas/response.py
from pydantic import BaseModel
from typing import Optional, Dict

class FingerPosition(BaseModel):
    fretboard: Optional[int]
    string: Optional[int]

class DetectResponse(BaseModel):
    detection_done: bool
    stable_count: int
    finger_positions: Dict[str, FingerPosition]
