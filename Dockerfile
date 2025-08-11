# ---------- Build stage (Gradle + JDK 17) ----------
FROM gradle:8.7-jdk17 AS build
WORKDIR /app

# 캐시 최적화를 위해 먼저 빌드 스크립트/래퍼만 복사
COPY build.gradle settings.gradle* gradle.properties* ./
COPY gradle gradle
# 의존성만 먼저 내려 캐시 레이어 확보
RUN gradle --no-daemon dependencies > /dev/null 2>&1 || true

# 실제 소스 복사 후 빌드
COPY . .
# Spring Boot 3.x 기본 jar 생성 태스크
RUN gradle bootJar --no-daemon

# ---------- Runtime stage (JRE 17) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 산출물 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 타임존 및 자바 기본 옵션
ENV TZ=Asia/Seoul \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Seoul"

EXPOSE 8080

# Actuator를 아직 안 쓰고 있으므로 루트( / )로 헬스체크
# (만약 actuator 추가하면 /actuator/health 로 바꾸세요)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]