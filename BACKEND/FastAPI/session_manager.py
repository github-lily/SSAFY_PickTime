# session_manager.py
import uuid
from inference import GuitarTracker

# 전역 세션 저장소 – 각 세션별로 GuitarTracker 인스턴스가 저장됩니다.
SESSION_DATA = {}

def create_session() -> str:
    session_id = str(uuid.uuid4())
    SESSION_DATA[session_id] = GuitarTracker()
    return session_id

def get_session(session_id: str):
    return SESSION_DATA.get(session_id)

def remove_session(session_id: str) -> None:
    SESSION_DATA.pop(session_id, None)
