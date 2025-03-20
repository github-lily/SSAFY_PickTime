# 🔹 1단계: 빌드용 이미지 (Gradle 사용하여 JAR 빌드)
FROM openjdk:17-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트의 모든 파일 복사
COPY . .

# 실행 권한 부여
RUN chmod +x gradlew

# Gradle 빌드 수행
RUN ./gradlew clean bootJar

# 🔹 2단계: 실행용 이미지 (Spring Boot 실행 환경)
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사 (정확한 경로 지정)
COPY --from=builder /app/build/libs/*.jar app.jar

# JAR 파일 확인 (디버깅용)
RUN ls -al /app/

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
