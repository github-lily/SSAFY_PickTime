import os
from fastapi import UploadFile

# 저장할 디렉터리 경로 (없으면 미리 생성해야 함)
SAVE_DIR = "uploaded_images"
os.makedirs(SAVE_DIR, exist_ok=True)

def save_upload_file(upload_file: UploadFile, destination: str):
    with open(destination, "wb") as buffer:
        content = upload_file.file.read()
        buffer.write(content)
