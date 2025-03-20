# 🔹 1단계: 빌드용 이미지 (Gradle 사용하여 JAR 빌드)
FROM openjdk:17-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 프로젝트 설정 파일 복사
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

# 소스 코드 복사
COPY src src

# 실행 권한 부여 및 빌드 수행
RUN chmod +x gradlew
RUN ./gradlew clean bootJar

# 🔹 2단계: 실행용 이미지 (Spring Boot 실행 환경)
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
