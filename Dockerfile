# syntax=docker/dockerfile:1

# ---- build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 캐시: 빌드 스크립트만 먼저 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
# 테스트는 Testcontainers(Docker)가 필요해서 이미지 빌드에서는 건너뛴다. 테스트는 PR 검사(CI)에서 돈다.
RUN ./gradlew --no-daemon bootJar -x test \
 && find build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /app/app.jar \;

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 deary
COPY --from=build /app/app.jar /app/app.jar
USER deary

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
