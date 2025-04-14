# main.py
import uvicorn
from fastapi import FastAPI
from router import api_router

def create_app() -> FastAPI:
    app = FastAPI(
        title="Guitar Detection Server",
        description="YOLO+Mediapipe 기반 기타 fret/string 검출 및 손가락 위치 추적",
        version="0.1"
    )
    print("실행합니다.")
    app.include_router(api_router, prefix="/ai")
    return app

app = create_app()

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)