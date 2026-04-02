# =============================================================================
# 멀티스테이지 Dockerfile: JDK builder + Node frontend-builder + JRE runtime
# =============================================================================

# --- Stage 1: Spring Boot 백엔드 빌드 ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY backend/gradle/ gradle/
COPY backend/gradlew backend/settings.gradle.kts backend/build.gradle.kts ./
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
COPY backend/src/ src/
RUN ./gradlew bootJar --no-daemon

# --- Stage 2: React 프론트엔드 빌드 ---
FROM node:22-alpine AS frontend-builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# --- Stage 3: 런타임 (JRE만 포함) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=frontend-builder /app/dist/ /app/static/
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
