# Build stage
FROM gradle:8-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-noble

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY yt-dlp.conf /etc/yt-dlp.conf

# Upgrade deps
RUN apt-get update && apt-get -y upgrade

# Install base dependencies
RUN export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends curl xattr \
    python3-minimal python-is-python3 python3-pip \
    python3-certifi python3-websockets python3-requests python3-brotli \
    python3-mutagen ffmpeg atomicparsley \
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

EXPOSE 8090

# Set the active profile through environment variable
ENV SPRING_PROFILES_ACTIVE=prod
ENV KAFKA_BOOTSTRAP_SERVERS=redpanda:9092

ENTRYPOINT ["java", "-jar", "app.jar"]
