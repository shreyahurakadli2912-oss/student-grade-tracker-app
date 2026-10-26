# ===================================================================
# Multi-Stage Dockerfile for Student Grade Management System
# Produces a single, lightweight, production-ready runnable container
# ===================================================================

# Stage 1: Build React Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci || npm install

COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend with Frontend Static Assets
FROM maven:3.9.9-eclipse-temurin-21 AS backend-builder
WORKDIR /app

# Copy POM and preload dependencies
COPY backend/pom.xml ./backend/
RUN mvn -f backend/pom.xml dependency:go-offline -B || true

# Copy compiled frontend dist into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist ./backend/src/main/resources/static/

# Copy backend source code and build production JAR
COPY backend/src ./backend/src/
RUN mvn -f backend/pom.xml clean package -DskipTests -B

# Stage 3: Minimal Secure Production Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Persistent storage folder for H2 file database fallback
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

COPY --from=backend-builder --chown=appuser:appgroup /app/backend/target/*.jar /app/app.jar

USER appuser

ENV PORT=8080
EXPOSE 8080

# Production Health Check
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/api/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
