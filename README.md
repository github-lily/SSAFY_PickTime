# 피크타임(PickTime)
![PickTime](https://github.com/user-attachments/assets/78dfd822-5b3a-4379-9e43-27793e99d1d1)

## 개발기간
| 개발기간 | 2025.02.24 ~ 2025.04.11 |
|----------|-------------------------|

## 팀 구성원
| 역할       | 이름   | 담당 업무 |
|------------|--------|---------------------------------------------------------------|
| FE,PM,발표자| 이현희 | 1)JWT 기반 인증/인가 기능 구현<br>2)연습모드 전체 흐름 및 피드백 로직 구현<br>3)FastAPI 기반 AI 분석 연동 (실시간 프레임 캡처 및 전처리 포함)<br>4)기타 코드·음정 인식 및 정확도 판별, 실시간 피드백 로직 구현<br>5)전체 UI/UX 및 캐릭터 디자인 |
| FE         | 김민정 | 1)코드 애니메이션 구현<br>2)게임모드 구현<br>3)마이페이지 구현<br>4)FastAPI 연동<br>5)캐릭터 및 전체 디자인 |
| BE/FE      | 이병조 | 1)튜닝 페이지 구현<br>2)주파수 분석 기능 구현<br>3)게임 및 연습모드 API 구현 |
| BE/AI      | 박성근 | 1)Spring Security + JWT 기반 인증/인가 기능 구현<br>2)이메일 인증 및 비밀번호 찾기 API 구현<br>3)YOLOv8-seg 모델 파인튜닝<br>4)YOLOv8-seg + MediaPipe 모델 기반 AI 기타 코드 인식 기능 구현 |
| INFRA/DATA,팀장 | 송용인 | 1)Jenkins 및 Docker 기반 CI/CD 파이프라인 구축<br>2)개발/배포 서버 분리 구축<br>3)AWS S3 환경 구축 및 연동<br>4)프로젝트 관련 데이터 수집 및 DB 삽입 |

## 핵심 기능
### 1) **코드 연습 모드**
- 영상 및 음향 분석 기술을 활용하여 사용자의 코드 운지법 및 연주 정확도 분석
- 잘못된 운지법 교정 (예: "더 세게 눌러보세요”, “다른 줄을 눌렀어요 다시 짚어볼까요?")
- 기타 초급자 맞춤 학습 커리큘럼 제공
- 정해진 하루 학습을 완료하면 캘린더에 체크 표시
### 2) **게임 모드**
- 사용자는 제공하는 노래에 맞춰 연주 후 점수를 계산하여 사용자에게 점수 제공

## 부가 기능
### **기타 튜닝**
- 마이크를 활용하여 **기타 현의 주파수를 분석**하고 **튜닝** 기능 지원
- 시각적 피드백 제공으로 사용자가 쉽게 조율 가능

## 기술 스택
## 기술 스택

### Management Tool

![gitlab](https://img.shields.io/badge/gitlab-FC6D26?style=for-the-badge&logo=gitlab&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![jira](https://img.shields.io/badge/jira-0052CC?style=for-the-badge&logo=jira&logoColor=white)
![notion](https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![figma](https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)

### IDE

![intellij](https://img.shields.io/badge/intellij_idea-000000?style=for-the-badge&logo=intellijidea&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)
![vscode](https://img.shields.io/badge/vscode-0078d7?style=for-the-badge&logo=visual%20studio&logoColor=white)
![postman](https://img.shields.io/badge/postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

### Infra

![ubuntu](https://img.shields.io/badge/ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![nginx](https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![docker](https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![docker-compose](https://img.shields.io/badge/Docker_Compose-393E46?style=for-the-badge&logo=docker&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)


### Frontend

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)



### Backend

![java](https://img.shields.io/badge/Java-007396?style=for-the-badge)
![springboot](https://img.shields.io/badge/spring%20boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![springjpa](https://img.shields.io/badge/spring%20jpa-6DB33F?style=for-the-badge&logo=Spring&logoColor=white)
![springsecurity](https://img.shields.io/badge/spring%20security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![jwt](https://img.shields.io/badge/jwt-000000?style=for-the-badge&logo=jwt&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)
![mysql](https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

### AI

![YOLOv8-seg](https://img.shields.io/badge/YOLOv8--seg-FF6F00?style=for-the-badge&logo=opencv&logoColor=white)
![MediaPipe](https://img.shields.io/badge/MediaPipe-FF6F00?style=for-the-badge&logo=google&logoColor=white)


## 시스템 아키텍처
![image](https://github.com/user-attachments/assets/7aa80af0-360d-47f4-9a9d-9580dd5ac818)

## PickTime 프로젝트 영상
https://drive.google.com/file/d/1Y0mp7etNGPKNpIxRFwrgdx76qUe4CJiZ/view?usp=drive_link

