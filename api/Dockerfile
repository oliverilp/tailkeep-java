# Build stage
FROM gradle:8-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV KAFKA_BOOTSTRAP_SERVERS=redpanda:9092

ENTRYPOINT ["java", "-jar", "app.jar"]
