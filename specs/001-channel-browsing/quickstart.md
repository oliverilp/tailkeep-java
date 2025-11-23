# Quickstart Guide: Channel Browsing Implementation

**Feature**: Channel Browsing  
**Branch**: `001-channel-browsing`  
**Date**: November 23, 2025

## Overview

This guide provides step-by-step instructions for implementing the channel browsing feature. The implementation adds REST API endpoints to browse channels and view channel-specific videos.

**Prerequisites**:
- Existing `Channel` and `Video` entities in database
- Spring Boot API service running
- Basic understanding of Spring MVC patterns

---

## Implementation Steps

### Step 1: Create DTOs

#### 1.1 Create `ChannelDetailDto`

**File**: `api/src/main/java/org/tailkeep/api/dto/ChannelDetailDto.java`

```java
package org.tailkeep.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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

**Purpose**: Represents a channel with video count for list views

---

#### 1.2 Create `ChannelWithVideosDto`

**File**: `api/src/main/java/org/tailkeep/api/dto/ChannelWithVideosDto.java`

```java
package org.tailkeep.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private Page<VideoDto> videos;
}
```

**Purpose**: Represents a channel with paginated video list for detail view

**Note**: Uses Spring's `Page<VideoDto>` directly - Jackson will serialize it properly

---

### Step 2: Create Repository

#### 2.1 Create `ChannelRepository`

**File**: `api/src/main/java/org/tailkeep/api/repository/ChannelRepository.java`

```java
package org.tailkeep.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tailkeep.api.model.Channel;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    Optional<Channel> findByYoutubeId(String youtubeId);
}
```

**Purpose**: Data access layer for Channel entity

**Methods**:
- `findAll(Pageable)` - Inherited from JpaRepository
- `findById(UUID)` - Inherited from JpaRepository  
- `findByYoutubeId(String)` - Custom query method

---

### Step 3: Update EntityMapper

#### 3.1 Add Channel Mapping Methods

**File**: `api/src/main/java/org/tailkeep/api/mapper/EntityMapper.java`

Add the following method to the existing interface:

```java
@Mapping(target = "videoCount", expression = "java(channel.getVideos() != null ? (long) channel.getVideos().size() : 0L)")
ChannelDetailDto toDetailDto(Channel channel);
```

**Purpose**: Maps Channel entity to ChannelDetailDto with video count

**Note**: The `toDto(Channel)` method already exists - don't duplicate it

---

### Step 4: Create Service

#### 4.1 Create `ChannelService`

**File**: `api/src/main/java/org/tailkeep/api/service/ChannelService.java`

```java
package org.tailkeep.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.dto.ChannelDetailDto;
import org.tailkeep.api.dto.ChannelWithVideosDto;
import org.tailkeep.api.dto.VideoDto;
import org.tailkeep.api.exception.ResourceNotFoundException;
import org.tailkeep.api.mapper.EntityMapper;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.repository.ChannelRepository;
import org.tailkeep.api.repository.VideoRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public Page<ChannelDetailDto> getAllChannels(Pageable pageable) {
        return channelRepository.findAll(pageable)
            .map(mapper::toDetailDto);
    }

    @Transactional(readOnly = true)
    public ChannelWithVideosDto getChannelWithVideos(UUID id, Pageable pageable) {
        Channel channel = channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Channel", id.toString()));

        // Create pageable with default sort for videos (newest first)
        Pageable videoPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<VideoDto> videos = videoRepository.findByChannel(channel, videoPageable)
            .map(mapper::toDto);

        return ChannelWithVideosDto.builder()
            .id(channel.getId())
            .name(channel.getName())
            .youtubeId(channel.getYoutubeId())
            .channelUrl(channel.getChannelUrl())
            .createdAt(channel.getCreatedAt())
            .updatedAt(channel.getUpdatedAt())
            .videos(videos)
            .build();
    }
}
```

**Key Points**:
- `@Transactional(readOnly = true)` for read operations (performance optimization)
- Default sort for videos: newest first (`createdAt DESC`)
- Throws `ResourceNotFoundException` if channel not found (consistent with VideoService)
- Manual construction of `ChannelWithVideosDto` (MapStruct can't handle pagination)

---

### Step 5: Update VideoRepository

#### 5.1 Add Pagination Support for findByChannel

**File**: `api/src/main/java/org/tailkeep/api/repository/VideoRepository.java`

Add overloaded method:

```java
Page<Video> findByChannel(Channel channel, Pageable pageable);
```

**Purpose**: Fetch videos for a channel with pagination support

**Complete interface**:

```java
@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    Optional<Video> findByYoutubeId(String youtubeId);
    List<Video> findByChannel(Channel channel);
    Page<Video> findByChannel(Channel channel, Pageable pageable);  // NEW
}
```

---

### Step 6: Create Controller

#### 6.1 Create `ChannelController`

**File**: `api/src/main/java/org/tailkeep/api/controller/ChannelController.java`

```java
package org.tailkeep.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tailkeep.api.dto.ChannelDetailDto;
import org.tailkeep.api.dto.ChannelWithVideosDto;
import org.tailkeep.api.service.ChannelService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<Page<ChannelDetailDto>> getAllChannels(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name,asc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        return ResponseEntity.ok(channelService.getAllChannels(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelWithVideosDto> getChannelById(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(channelService.getChannelWithVideos(id, pageable));
    }
}
```

**Key Points**:
- Follows exact same pattern as `VideoController`
- Default pagination: page 0, size 20
- Default sort: `name,asc` for channel list (alphabetical A-Z)
- Videos always sorted newest first (handled in service layer)
- Returns `ResponseEntity` for consistent HTTP handling

---

## Testing the Implementation

### Manual Testing with cURL

#### Test 1: Get All Channels (Default Pagination)

```bash
curl -X GET 'http://localhost:8080/api/v1/channels' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  | jq
```

**Expected**: Paginated list of channels with video counts

---

#### Test 2: Get All Channels (Custom Pagination)

```bash
curl -X GET 'http://localhost:8080/api/v1/channels?page=0&size=10' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  | jq
```

**Expected**: First 10 channels sorted alphabetically (A-Z by default)

---

#### Test 3: Get Channel Details with Videos

```bash
curl -X GET 'http://localhost:8080/api/v1/channels/{CHANNEL_ID}' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  | jq
```

**Expected**: Channel details with first 20 videos (newest first)

---

#### Test 4: Get Channel Videos (Page 2)

```bash
curl -X GET 'http://localhost:8080/api/v1/channels/{CHANNEL_ID}?page=1&size=10' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  | jq
```

**Expected**: Channel details with videos 11-20

---

#### Test 5: Non-Existent Channel (404 Test)

```bash
curl -X GET 'http://localhost:8080/api/v1/channels/00000000-0000-0000-0000-000000000000' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -i
```

**Expected**: HTTP 404 with error message

---

### Integration Testing (Optional)

If tests are required, create integration tests following existing patterns:

**File**: `api/src/test/java/org/tailkeep/api/integration/ChannelControllerIntegrationTest.java`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChannelControllerIntegrationTest {
    // Test getAllChannels returns paginated results
    // Test getChannelById returns channel with videos
    // Test getChannelById throws 404 for non-existent channel
    // Test pagination works correctly
}
```

**Note**: Tests are optional per constitution - only implement if explicitly requested

---

## Verification Checklist

- [ ] `ChannelDetailDto` record created
- [ ] `ChannelWithVideosDto` class created  
- [ ] `ChannelRepository` interface created
- [ ] `EntityMapper.toDetailDto(Channel)` method added
- [ ] `VideoRepository.findByChannel(Channel, Pageable)` method added
- [ ] `ChannelService` class created with both methods
- [ ] `ChannelController` class created with both endpoints
- [ ] Application compiles without errors (`./gradlew build`)
- [ ] API service starts successfully (`./gradlew bootRun`)
- [ ] GET /api/v1/channels returns channel list
- [ ] GET /api/v1/channels/{id} returns channel with videos
- [ ] Pagination works for both endpoints
- [ ] 404 error returned for non-existent channel

---

## Common Issues & Solutions

### Issue 1: Lazy Loading Exception

**Symptom**: `LazyInitializationException` when accessing `channel.getVideos()`

**Solution**: Ensure `@Transactional(readOnly = true)` on service methods OR use explicit query with pagination

---

### Issue 2: Circular Reference in JSON

**Symptom**: Jackson infinite recursion when serializing Video → Channel → Videos

**Solution**: Already handled - `ChannelWithVideosDto` has `Page<VideoDto>` where `VideoDto` contains simplified `ChannelDto` (no video list)

---

### Issue 3: N+1 Query Problem

**Symptom**: One query per channel to count videos

**Solution**: For MVP, this is acceptable. Future optimization: Use JPQL with `LEFT JOIN FETCH` or native query with `COUNT()`

---

### Issue 4: Video Sort Not Applied

**Symptom**: Videos returned in random order

**Solution**: Ensure `ChannelService.getChannelWithVideos()` creates `Pageable` with `Sort.by(Sort.Direction.DESC, "createdAt")`

---

## File Summary

**New Files** (7):
1. `api/src/main/java/org/tailkeep/api/dto/ChannelDetailDto.java`
2. `api/src/main/java/org/tailkeep/api/dto/ChannelWithVideosDto.java`
3. `api/src/main/java/org/tailkeep/api/repository/ChannelRepository.java`
4. `api/src/main/java/org/tailkeep/api/service/ChannelService.java`
5. `api/src/main/java/org/tailkeep/api/controller/ChannelController.java`

**Modified Files** (2):
1. `api/src/main/java/org/tailkeep/api/mapper/EntityMapper.java` - Add `toDetailDto(Channel)` method
2. `api/src/main/java/org/tailkeep/api/repository/VideoRepository.java` - Add `findByChannel(Channel, Pageable)` method

---

## Next Steps

After implementation:
1. Test all endpoints manually with Postman/cURL
2. Verify pagination works correctly
3. Check performance with large datasets (100+ videos per channel)
4. Frontend integration (out of scope for this backend-focused task)
5. Consider adding filters (search by channel name) in future iteration

---

## Additional Resources

- [Spring Data JPA Pagination](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)
- [OpenAPI Specification](../contracts/channel-api.yaml)
- [Feature Specification](../spec.md)
- [Data Model](../data-model.md)

