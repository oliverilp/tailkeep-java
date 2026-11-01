# Web build stage. The output is the same on every platform, so it runs only once.
FROM --platform=$BUILDPLATFORM node:22-alpine AS web-builder

WORKDIR /web
COPY web/package.json web/pnpm-lock.yaml ./
RUN corepack enable pnpm && pnpm i --frozen-lockfile
COPY web/ .
RUN pnpm run build

# Build stage
FROM gradle:8-jdk21 AS builder

WORKDIR /app/backend
COPY backend/ .
COPY --from=web-builder /web/dist ../web/dist
RUN gradle build -x test --no-daemon

# JS runtime for yt-dlp's YouTube extraction
FROM node:26.13.0-trixie-slim AS node

# Runtime stage
FROM eclipse-temurin:21-jre-noble

WORKDIR /app
COPY --from=builder /app/backend/mod-application/build/libs/*.jar app.jar
COPY --from=node /usr/local/bin/node /usr/local/bin/node
COPY backend/yt-dlp.conf /etc/yt-dlp.conf

# Upgrade deps
RUN apt-get update && apt-get -y upgrade

# Install base dependencies
RUN export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends curl xattr \
    python3-minimal python-is-python3 python3-pip \
    python3-certifi python3-websockets python3-requests python3-brotli \
    python3-mutagen ffmpeg atomicparsley libatomic1 \
    && apt-get autoremove -y --purge \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install youtube-dl
RUN mkdir -p /home/root/.local/bin \
    && curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /home/root/.local/bin/yt-dlp \
    && chmod a+rx /home/root/.local/bin/yt-dlp  # Make executable

# Add youtube-dl to global path
RUN test -e /home/root/.local/bin/yt-dlp \
    && ln -s /home/root/.local/bin/yt-dlp /usr/local/bin/yt-dlp 

EXPOSE 8080

# Set the active profile through environment variable
ENV SPRING_PROFILES_ACTIVE=prod
ENV KAFKA_BOOTSTRAP_SERVERS=redpanda:9092
ENV MEDIA_PATH=/videos

ENTRYPOINT ["java", "-jar", "app.jar"]
