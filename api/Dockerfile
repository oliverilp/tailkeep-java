# Build stage
FROM gradle:8-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Set the active profile through environment variable
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
