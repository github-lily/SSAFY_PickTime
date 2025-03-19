# jdk 기본 이미지 사용
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정 (/app 디렉토리를 컨테이너 내부의 작업 경로로 지정)
WORKDIR /app

# JAR_FILE이라는 변수를 선언하고 기본값을 build/libs/*.jar로 설정.
ARG JAR_FILE=build/libs/*.jar
# 로컬 JAR 파일을 컨테이너 내부로 복사
# 빌드된 .jar 파일을 컨테이너 내부 /app/app.jar로 복사.
COPY ${JAR_FILE} app.jar

# SpringBoot 컨테이너 내부 포트
EXPOSE 8080

# SpringBoot 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]