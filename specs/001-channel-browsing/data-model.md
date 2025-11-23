# Data Model: Channel Browsing

**Feature**: Channel Browsing  
**Date**: November 23, 2025  
**Phase**: 1 - Design

## Overview

This document defines the data structures for the channel browsing feature. Most entities already exist in the database; this feature primarily adds new DTOs for API responses.

---

## Entities (Database Models)

### Channel (EXISTS - No Changes Required)

**Table**: `channel`  
**Purpose**: Represents a video content source/creator

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique identifier |
| `name` | String | NOT NULL | Channel display name |
| `youtube_id` | String | NOT NULL, UNIQUE | YouTube channel ID |
| `channel_url` | String | NOT NULL, UNIQUE | Full URL to channel |
| `created_at` | LocalDateTime | NOT NULL, AUTO | Record creation timestamp |
| `updated_at` | LocalDateTime | NOT NULL, AUTO | Last update timestamp |

**Relationships**:
- One-to-Many with `Video` (mapped by `channel` field in Video entity)

**Indexes**:
- Primary key on `id`
- Unique index on `youtube_id`
- Unique index on `channel_url`

**Notes**: All required fields exist. No schema migration needed.

---

### Video (EXISTS - No Changes Required)

**Table**: `video`  
**Purpose**: Represents individual video content

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique identifier |
| `youtube_id` | String | NOT NULL, UNIQUE | YouTube video ID |
| `channel_id` | UUID | NOT NULL, FOREIGN KEY | Reference to Channel |
| `url` | String | NOT NULL | Video URL |
| `title` | String | NOT NULL | Video title |
| `duration_string` | String | NULLABLE | Human-readable duration |
| `duration` | Double | NULLABLE | Duration in seconds |
| `thumbnail_url` | String | NULLABLE | Thumbnail image URL |
| `description` | Text | NULLABLE | Video description |
| `view_count` | Long | NULLABLE | Number of views |
| `comment_count` | Long | NULLABLE | Number of comments |
| `filename` | String | NOT NULL | Stored file name |
| `created_at` | LocalDateTime | NOT NULL, AUTO | Record creation timestamp |
| `updated_at` | LocalDateTime | NOT NULL, AUTO | Last update timestamp |

**Relationships**:
- Many-to-One with `Channel` (via `channel_id`)
- One-to-Many with `DownloadProgress` (not relevant for this feature)

**Indexes**:
- Primary key on `id`
- Foreign key on `channel_id`
- Unique index on `youtube_id`

**Notes**: All required fields exist. No schema migration needed.

---

## DTOs (Data Transfer Objects)

### ChannelDto (EXISTS - No Changes Required)

**Purpose**: Basic channel information for list views  
**Type**: Java Record (immutable)  
**Usage**: Channel list responses, nested in VideoDto

```java
public record ChannelDto(
    UUID id,
    String name,
    String youtubeId,
    String channelUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**Validation**: None required (read-only)

**Example JSON**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Example Channel",
  "youtubeId": "UCxxxxxxxxxxxxxx",
  "channelUrl": "https://www.youtube.com/channel/UCxxxxxxxxxxxxxx",
  "createdAt": "2025-11-01T10:30:00",
  "updatedAt": "2025-11-20T15:45:00"
}
```

---

### ChannelDetailDto (NEW)

**Purpose**: Channel information with video count for list views  
**Type**: Java Record (immutable)  
**Usage**: GET /api/v1/channels response (paginated list)

```java
public record ChannelDetailDto(
    UUID id,
    String name,
    String youtubeId,
    String channelUrl,
    Long videoCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**Fields**:
- All fields from `ChannelDto`
- `videoCount`: Number of videos belonging to this channel

**Mapping**: MapStruct custom mapping to include `COUNT(videos)`

**Validation**: None required (read-only)

**Example JSON**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Example Channel",
  "youtubeId": "UCxxxxxxxxxxxxxx",
  "channelUrl": "https://www.youtube.com/channel/UCxxxxxxxxxxxxxx",
  "videoCount": 42,
  "createdAt": "2025-11-01T10:30:00",
  "updatedAt": "2025-11-20T15:45:00"
}
```

---

### ChannelWithVideosDto (NEW)

**Purpose**: Channel details with paginated video list  
**Type**: Java Class (mutable for builder pattern)  
**Usage**: GET /api/v1/channels/{id} response

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelWithVideosDto {
    private UUID id;
    private String name;
    private String youtubeId;
    private String channelUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PageResponse<VideoDto> videos;
}
```

**Fields**:
- All basic channel fields from `ChannelDto`
- `videos`: Paginated list of videos belonging to this channel

**Nested Type**: `PageResponse<VideoDto>` (custom pagination wrapper)

**Validation**: None required (read-only)

**Example JSON**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Example Channel",
  "youtubeId": "UCxxxxxxxxxxxxxx",
  "channelUrl": "https://www.youtube.com/channel/UCxxxxxxxxxxxxxx",
  "createdAt": "2025-11-01T10:30:00",
  "updatedAt": "2025-11-20T15:45:00",
  "videos": {
    "content": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "title": "Video 1",
        "duration": 180.5,
        ...
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

---

### PageResponse<T> (CHECK IF EXISTS)

**Purpose**: Generic pagination metadata wrapper  
**Type**: Generic Java Record  
**Usage**: Wrap paginated responses across API

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
```

**Note**: Check if similar class exists in codebase. If Spring's `Page<T>` is returned directly, may not need custom wrapper.

**Alternative**: Return Spring's `Page<T>` directly and let Jackson serialize it

---

## DTO Mapping Strategy

### MapStruct Mappings (EntityMapper Updates)

**Add to EntityMapper interface**:

```java
// Basic channel mapping (already exists)
ChannelDto toDto(Channel channel);

// Channel with video count - requires custom mapping
@Mapping(target = "videoCount", expression = "java(channel.getVideos() != null ? (long) channel.getVideos().size() : 0L)")
ChannelDetailDto toDetailDto(Channel channel);

// Channel with videos - manual construction in service layer
// (Cannot be done via MapStruct due to pagination complexity)
```

**Service Layer Responsibility**:
- `ChannelService.getChannelWithVideos(UUID id, Pageable pageable)` constructs `ChannelWithVideosDto` manually
- Fetches channel entity, maps to DTO fields, fetches paginated videos separately

---

## State Transitions

### Channel Lifecycle

Channels are relatively static:
1. **Created**: Channel added when first video is downloaded from it
2. **Updated**: `updated_at` timestamp refreshed when metadata changes
3. **Deleted**: Soft delete via `deleted_at` column (not implemented yet)

**No State Machine Needed**: Channels don't have workflow states

---

## Ordering Rules

### Channel List View

**Default Sort**: `name` ascending (A-Z alphabetically)

**Rationale**:
- Alphabetical ordering makes browsing and finding channels intuitive
- Standard expectation for directory-style listings
- Easy to scan and locate specific channels by name

### Videos Within Channel Detail View

**Default Sort**: `createdAt` descending (newest first)

**Rationale**:
- Matches existing `VideoService.getAllVideos()` behavior
- Users expect newest content first
- Consistent with video platform conventions

**Future Enhancement**: Allow client to specify sort field/direction via query params

---

## Validation Rules

### Channel Entity

- `name`: Required, max 255 characters
- `youtube_id`: Required, unique, max 100 characters
- `channel_url`: Required, unique, valid URL format

**Note**: Validation happens at entity level (JPA annotations) and during creation (not part of this feature)

### Video Entity

- `title`: Required, max 500 characters
- `youtube_id`: Required, unique, max 100 characters
- `channel_id`: Required, must reference existing channel

**Note**: This feature only reads videos, no validation needed

---

## Performance Considerations

### Lazy vs Eager Loading

**Channel.videos Relationship**:
- Marked as `@OneToMany(mappedBy = "channel")` - **lazy by default**
- Do NOT load all videos when fetching channel list
- Explicitly fetch videos with pagination when viewing channel detail

**Video.channel Relationship**:
- Marked as `@ManyToOne` with `@JoinColumn` - **eager by default**
- Each video loads its channel info (acceptable for video lists)

### N+1 Query Prevention

**Channel List**: Use `LEFT JOIN` with `COUNT` to get video counts in single query
**Channel Detail**: Fetch channel + paginated videos in 2 queries (acceptable)

---

## API Response Examples

### GET /api/v1/channels?page=0&size=20

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Tech Reviews",
      "youtubeId": "UCxxxxxxxxxxxxxx",
      "channelUrl": "https://www.youtube.com/channel/UCxxxxxxxxxxxxxx",
      "videoCount": 42,
      "createdAt": "2025-11-01T10:30:00",
      "updatedAt": "2025-11-20T15:45:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### GET /api/v1/channels/{id}?page=0&size=20

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tech Reviews",
  "youtubeId": "UCxxxxxxxxxxxxxx",
  "channelUrl": "https://www.youtube.com/channel/UCxxxxxxxxxxxxxx",
  "createdAt": "2025-11-01T10:30:00",
  "updatedAt": "2025-11-20T15:45:00",
  "videos": {
    "content": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "youtubeId": "dQw4w9WgXcQ",
        "channel": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "name": "Tech Reviews",
          ...
        },
        "title": "Latest Gadget Review",
        "durationString": "10:30",
        "duration": 630.0,
        "thumbnailUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
        "description": "In this video...",
        "viewCount": 10000,
        "commentCount": 250,
        "filename": "latest-gadget-review.mp4",
        "createdAt": "2025-11-20T15:45:00",
        "updatedAt": "2025-11-20T15:45:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

---

## Summary

### Existing Entities (No Changes)
- ✅ `Channel` entity complete
- ✅ `Video` entity complete
- ✅ Relationships defined
- ✅ Database schema ready

### New DTOs Required
- ✅ `ChannelDetailDto` - Channel with video count
- ✅ `ChannelWithVideosDto` - Channel with video list
- ⚠️  `PageResponse<T>` - Check if exists, create if needed

### Mapping Updates
- ✅ Add `toDetailDto(Channel)` to EntityMapper
- ✅ Manual construction of `ChannelWithVideosDto` in service layer

**Next Phase**: Create API contracts (OpenAPI specification)

