# --- build stage ---
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# 래퍼/빌드 스크립트 먼저 복사해서 의존성 레이어를 캐시
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
# 테스트는 DB가 필요하므로 이미지 빌드 단계에서는 제외
RUN ./gradlew --no-daemon bootJar -x test

# --- runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --create-home --shell /bin/bash spring
USER spring

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
