# ----------------------------------------
# 1단계: Build Stage
# ----------------------------------------
FROM gradle:8.5-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /home/devuser/app

# 소스코드 복사
COPY . .

# 빌드 수행
RUN gradle build -x test --no-daemon

# ----------------------------------------
# 2단계: Run Stage (최종 이미지)
# ----------------------------------------
# OpenJDK 21 기반 이미지 사용
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /home/devuser/app

# 빌드된 jar 파일 복사
COPY --from=builder /home/devuser/app/build/libs/*.jar app.jar

# 컨테이너 실행 시 jar 실행
ENTRYPOINT ["java", "-jar", "app.jar"]