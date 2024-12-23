# Tailkeep - YouTube Video Archiver

[Demo Website](https://demo.tailkeep.org/)

## Description

Tailkeep is a free and open-source web application designed to help users download and archive YouTube videos. The primary motivation behind this project is to give users control over their data, especially in the context of the increasing tendency of big tech companies like YouTube to remove videos due to copyright strikes or other reasons. This is particularly relevant for music videos, song remixes, movie soundtracks and other content that might be taken down.

## Motivation

- **Control Over Data:** Users can have their own copies of videos, ensuring they are not lost due to removal from YouTube.
- **Preservation of Content:** Helps in archiving videos that might be removed due to copyright issues or creator's decisions.

## Features

- Download and archive YouTube videos.
- View and manage downloaded videos through a user-friendly dashboard.
- Store video metadata for easy retrieval and management.

## Architecture

The application is structured into four main components:

1. **API:** Handles the business logic and JWT authentication.
2. **Worker:** Manages the downloading and processing of videos in the background.
3. **Web:** React web UI.
4. **Media:** Go server used for serving static video and image files.

### Technologies Used

- **Java**
- **Spring Boot**
- **Lombok**
- **TypeScript**
- **React**
- **Next.js**
- **Tailwind CSS**
- **Server-Sent Events (SSE)**
- **PostgreSQL**
- **Apache Kafka**
- **PNPM**
- **Docker**
- **Traefik**

### Message Queues

The system uses Apache Kafka message queues for handling background tasks. This ensures that video downloading and processing tasks do not interfere with the main application’s performance.

### Database Schema

Below is the Entity Relationship Diagram showing the database structure:

```mermaid
%%{init: {'er': {'layoutDirection': 'LR'}}}%%
erDiagram
    User {
        UUID id PK
        String nickname
        String username
        String password
        Role role "USER|ADMIN"
        DateTime createdAt
        DateTime updatedAt
    }

    Token {
        UUID id PK
        String token
        TokenType tokenType "BEARER"
        Boolean revoked
        Boolean expired
        UUID user_id FK
    }

    JwtSecret {
        UUID id PK
        String secretKey
        DateTime createdAt
    }

    Channel {
        UUID id PK
        String name
        String youtubeId
        String channelUrl
        DateTime createdAt
        DateTime updatedAt
    }

    Video {
        UUID id PK
        String youtubeId
        UUID channel_id FK
        String url
        String title
        String durationString
        Double duration
        String thumbnailUrl
        String description
        Long viewCount
        Long commentCount
        String filename
        DateTime createdAt
        DateTime updatedAt
    }

    Job {
        UUID id PK
        String inputUrl
        UUID video_id FK
        DateTime createdAt
        DateTime updatedAt
    }

    DownloadProgress {
        UUID id PK
        UUID job_id FK
        UUID video_id FK
        String status
        Boolean hasEnded
        Double progress
        String size
        String speed
        String eta
        DateTime createdAt
        DateTime updatedAt
        DateTime completedAt
    }

    %% Relationships
    User ||--o{ Token : "has many"
    Channel ||--o{ Video : "has many"
    Video ||--o{ DownloadProgress : "has many"
    Video ||--o{ Job : "has many"
    Job ||--|| DownloadProgress : "has one"
```
