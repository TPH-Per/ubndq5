# ==============================================================================
# Dockerfile - Spring Boot Backend
# Multi-stage build: Maven build → JRE runtime
# Base: Eclipse Temurin 21 (Java 21 LTS)
# ==============================================================================

# ─── STAGE 1: BUILD ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cache Maven dependencies (layer caching khi chỉ pom.xml thay đổi)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy và build source
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# ─── STAGE 2: RUNTIME ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Cài thêm curl để dùng trong healthcheck
RUN apk add --no-cache curl

# Copy JAR từ build stage
COPY --from=build /app/target/*.jar app.jar

# Port backend đang dùng
EXPOSE 8081

# Environment mặc định (sẽ bị override bởi docker-compose)
ENV SPRING_PROFILES_ACTIVE=dev \
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/cholon_db \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres \
    JWT_SECRET=LocalDevSecretKeyChangeInProduction \
    JWT_EXPIRATION=604800000 \
    JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
