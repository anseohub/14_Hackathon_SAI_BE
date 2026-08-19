# 1단계: 빌드 (JDK 17로 Gradle 빌드)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar -x test --no-daemon

# 2단계: 실행 (JRE만 있는 가벼운 이미지에 빌드 결과물만 복사)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Railway가 주입하는 PORT 환경변수를 애플리케이션이 읽음 (application.properties의 server.port 참고)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
