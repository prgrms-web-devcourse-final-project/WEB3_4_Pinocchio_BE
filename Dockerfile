# OpenJDK 21 기반 이미지 사용
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 파일 복사
COPY build/libs/*.jar app.jar

# 컨테이너 실행 시 jar 실행
ENTRYPOINT ["java", "-jar", "app.jar"]