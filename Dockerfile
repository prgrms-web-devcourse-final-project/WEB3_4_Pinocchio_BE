# 1. Java 21 기반 이미지
FROM openjdk:21-jdk

# 2. 앱 실행 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 Docker 이미지 안으로 복사
COPY build/libs/*.jar app.jar

# 4. 컨테이너 실행 시 JAR 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/app/app.jar"]