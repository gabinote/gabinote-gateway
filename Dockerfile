# Multi-stage build를 사용하여 최적화된 이미지 생성
FROM eclipse-temurin:21-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 build.gradle.kts 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Gradle wrapper 실행 권한 부여
RUN chmod +x gradlew

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build -x test --no-daemon

# 프로덕션 이미지 생성 - distroless 이미지 사용
FROM gcr.io/distroless/java21-debian12:nonroot

# 메타데이터 설정
LABEL maintainer="gabinote"
LABEL version="1.0.0"
LABEL description="Gateway Manager API"

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 애플리케이션 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]