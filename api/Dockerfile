# Web build stage. The output is the same on every platform, so it runs only once.
FROM --platform=$BUILDPLATFORM node:22-alpine AS web-builder

WORKDIR /web
COPY web/package.json web/pnpm-lock.yaml ./
RUN corepack enable pnpm && pnpm i --frozen-lockfile
COPY web/ .
RUN pnpm run build

# Build stage
FROM gradle:8-jdk21 AS builder

WORKDIR /app/api
COPY api/ .
COPY --from=web-builder /web/dist ../web/dist
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/api/build/libs/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV KAFKA_BOOTSTRAP_SERVERS=redpanda:9092

ENTRYPOINT ["java", "-jar", "app.jar"]
