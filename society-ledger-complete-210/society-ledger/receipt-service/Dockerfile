FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build
# Copy parent pom and common-lib first for dependency caching
COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
COPY receipt-service/pom.xml receipt-service/pom.xml
RUN mvn dependency:go-offline -pl common-lib,receipt-service -am -q
COPY receipt-service/src receipt-service/src
RUN mvn package -pl common-lib,receipt-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S slapp && adduser -S slapp -G slapp
USER slapp
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
COPY --from=builder /build/receipt-service/target/*.jar app.jar
RUN mkdir -p /data/society-ledger
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
