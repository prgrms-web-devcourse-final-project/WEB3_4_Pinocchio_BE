# ----------------------------------------
# 1단계: Run Stage (최종 이미지)
# ----------------------------------------
# OpenJDK 21 기반 이미지 사용
FROM openjdk:21-jdk

# 작업 디렉토리 설정
WORKDIR /home/devuser/app

# 빌드된 jar 파일 복사
COPY /home/devuser/app/build/libs/*.jar app.jar

# 컨테이너 실행 시 jar 실행
ENTRYPOINT ["java", "-jar", "app.jar"]