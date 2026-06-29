# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy parent POM and common-lib first for dependency layer caching
COPY pom.xml ./pom.xml
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
COPY statement-service/pom.xml statement-service/pom.xml

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl common-lib,statement-service -am -q 2>/dev/null || true

# Build
COPY statement-service/src statement-service/src
RUN mvn package -pl common-lib,statement-service -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Security: non-root user
RUN addgroup -S slapp && adduser -S slapp -G slapp
USER slapp

# Create data directories
RUN mkdir -p /app/data

ENV JAVA_OPTS="-XX:+UseContainerSupport                -XX:MaxRAMPercentage=75.0                -XX:+UseG1GC                -Djava.security.egd=file:/dev/./urandom                -Dspring.profiles.active=default"

COPY --from=builder /workspace/statement-service/target/*.jar app.jar

EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3   CMD wget -qO- http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
