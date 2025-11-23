# Research & Technical Decisions: Channel Browsing

**Feature**: Channel Browsing  
**Date**: November 23, 2025  
**Status**: Phase 0 Complete

## Overview

This document captures technical decisions and research findings for implementing channel browsing endpoints. Since the database schema already exists and the implementation follows established patterns (VideoController/Service), minimal research was required.

## Key Technical Decisions

### 1. Video Sort Order Within Channels (FR-010 Clarification)

**Decision**: Sort videos by upload date, newest first (descending `createdAt`)

**Rationale**:
- Matches existing `VideoService.getAllVideos()` implementation (`Sort.by(Sort.Direction.DESC, "createdAt")`)
- Users typically want to see newest content first from channels they follow
- Consistent with common video platform behavior (YouTube, Vimeo, etc.)
- Simple to implement and efficient with proper database indexing

**Alternatives Considered**:
- **Oldest first**: Rejected - less intuitive for typical browsing behavior
- **Alphabetical**: Rejected - not relevant for time-based video content
- **User-configurable**: Rejected - adds unnecessary complexity for MVP, can be added later if needed

**Implementation**: Use Spring Data's `Sort.by(Sort.Direction.DESC, "createdAt")` when querying videos by channel

---

### 2. Channel List Sort Order

**Decision**: Sort channels alphabetically by name, ascending (A-Z)

**Rationale**:
- Alphabetical ordering is intuitive for browsing directory-style listings
- Users can easily find channels by scanning alphabetically
- Standard pattern for list views (contacts, file browsers, etc.)
- Name-based sorting is predictable and user-friendly

**Alternatives Considered**:
- **By creation date**: Rejected - not useful for finding specific channels
- **By video count**: Rejected - prioritizes quantity over discoverability
- **User-configurable**: Deferred - alphabetical is sufficient default for MVP

**Implementation**: Use Spring Data's `Sort.by(Sort.Direction.ASC, "name")` for channel list endpoint

---

### 3. Pagination Strategy

**Decision**: Use Spring Data's `Pageable` and `Page<T>` for both channel lists and channel video lists

**Rationale**:
- Spring Boot best practice for pagination
- Consistent with REST API standards
- Built-in support for page number, page size, sorting
- Returns metadata (total elements, total pages, current page)
- Zero custom implementation needed

**Implementation Details**:
- Default page size: 20 items (configurable via request parameter)
- Maximum page size: 100 items (prevent abuse)
- Query parameters: `page` (0-indexed), `size`, `sort` (optional)
- Response includes: `content`, `totalElements`, `totalPages`, `number`, `size`

**Example Request**: `GET /api/v1/channels?page=0&size=20`

---

### 4. DTO Design Strategy

**Decision**: Create three DTOs for different use cases:
1. `ChannelDto` (exists) - Basic channel info for lists
2. `ChannelDetailDto` (new) - Channel with video count, no video list
3. `ChannelWithVideosDto` (new) - Channel with paginated video list

**Rationale**:
- **Performance**: Avoid loading all videos when just listing channels
- **Flexibility**: Support both "channel list" and "channel detail" endpoints
- **KISS**: Each DTO has single, clear purpose
- **Consistency**: Mirrors VideoDto vs VideoByIdDto pattern

**Alternative Rejected**: Single DTO with optional fields - violates SRP, harder to understand API contract

---

### 5. Repository Pattern

**Decision**: Create `ChannelRepository extends JpaRepository<Channel, UUID>`

**Rationale**:
- Follows existing pattern (`VideoRepository`, `JobRepository`, etc.)
- Spring Data JPA provides CRUD operations automatically
- Custom queries can be added via method naming convention
- No need for custom implementation - interface-only

**Required Methods**:
- `findAll(Pageable)` - Provided by JpaRepository
- `findById(UUID)` - Provided by JpaRepository
- `findByYoutubeId(String)` - Custom query for YouTube ID lookup (if needed)

---

### 6. API Endpoint Design

**Decision**: RESTful endpoints following existing `/api/v1/*` pattern

**Endpoints**:
1. `GET /api/v1/channels` - List all channels (paginated)
   - Response: `Page<ChannelDetailDto>` (includes video count)
   - Sort: Alphabetically by name (A-Z)
2. `GET /api/v1/channels/{id}` - Get channel details with videos
   - Response: `ChannelWithVideosDto` containing `Page<VideoDto>`
   - Videos paginated separately via query params
   - Videos sorted: Newest first (descending by createdAt)

**Rationale**:
- Matches existing `/api/v1/videos` pattern
- RESTful resource naming
- Clean, predictable URLs
- Standard HTTP methods (GET only for read operations)
- Alphabetical channel sorting makes browsing intuitive
- Newest-first video sorting shows recent content

**Alternative Rejected**: `/api/v1/channels/{id}/videos` as separate endpoint - adds complexity without benefit for MVP

---

### 7. Error Handling

**Decision**: Reuse existing `ResourceNotFoundException` from VideoService pattern

**Rationale**:
- Consistent error responses across API
- Already mapped to HTTP 404 by exception handler
- No new exception types needed for standard CRUD operations

**Usage**: `throw new ResourceNotFoundException("Channel", id.toString())`

---

### 8. Security & Authorization

**Decision**: Apply same authentication requirements as VideoController

**Rationale**:
- Channels and videos are equivalent resources in terms of access control
- No special permissions needed for browsing public content
- JWT authentication already configured globally

**Implementation**: No additional security annotations needed beyond class-level if VideoController doesn't have them

---

## Dependencies & Libraries

### Existing (No New Dependencies)

- **Spring Data JPA**: Repository and pagination support
- **MapStruct**: DTO mapping
- **Lombok**: Boilerplate reduction
- **Spring Web**: REST controllers

### Not Required

- No new external dependencies needed
- All functionality available in existing Spring Boot stack

---

## Database Considerations

### Schema Status

✅ **All tables exist**: `channel` and `video` tables already created  
✅ **Relationships defined**: `video.channel_id` foreign key to `channel.id`  
✅ **Indexes**: Primary keys and foreign keys already indexed

### Potential Optimizations (Future)

- **Video Count**: Current approach queries `COUNT(videos)` via JPA relationship
  - For large datasets, consider: Materialized view or cached count column
  - **Decision**: Use simple JPA count for MVP - optimize only if performance issues arise
- **Pagination Index**: If channels grow to thousands, add index on `created_at` or `name`
  - **Decision**: Defer until needed - current scale doesn't warrant it

---

## Performance Considerations

### Query Optimization

**Channel List Query**:
```sql
SELECT c.*, COUNT(v.id) as video_count 
FROM channel c 
LEFT JOIN video v ON v.channel_id = c.id 
GROUP BY c.id 
ORDER BY c.name ASC 
LIMIT ? OFFSET ?
```

**Channel Detail Query**:
```sql
-- Fetch channel
SELECT * FROM channel WHERE id = ?

-- Fetch videos (paginated)
SELECT * FROM video WHERE channel_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?
```

**Expected Performance**:
- Channel list: <100ms for hundreds of channels
- Channel detail: <200ms for 100 videos
- Both well within success criteria (<2s and <3s respectively)

### Caching Strategy

**Decision**: No caching for MVP

**Rationale**:
- Video/channel data changes infrequently (only on downloads)
- Database queries are fast enough for success criteria
- Adds complexity without clear benefit at current scale
- Can be added later if needed (Spring Cache abstraction)

---

## Testing Strategy

Per constitution Section V (Testing Discipline):
- **Tests NOT required** for this feature (standard CRUD, no critical integration points)
- **If tests were added**, would cover:
  - Integration tests for controller endpoints
  - Service layer tests for business logic
  - Repository tests not needed (Spring Data JPA tested by Spring team)

---

## Open Questions & Future Enhancements

### Resolved
- ✅ Video sort order: Newest first (matches existing pattern)
- ✅ Pagination approach: Spring Data Pageable
- ✅ DTO structure: Three DTOs for different use cases

### Future Enhancements (Out of Scope)
- Custom sort options (by name, by video count)
- Channel search/filtering
- Channel metadata (avatar, banner, description) - requires schema migration
- Video count caching for performance
- Server-Sent Events (SSE) for real-time updates when new videos added

---

## Implementation Checklist

- [ ] Create `ChannelRepository` interface
- [ ] Create `ChannelDetailDto` record
- [ ] Create `ChannelWithVideosDto` class
- [ ] Update `EntityMapper` with channel mapping methods
- [ ] Create `ChannelService` with business logic
- [ ] Create `ChannelController` with REST endpoints
- [ ] Manual testing via Postman/curl
- [ ] Update API documentation (if maintaining separate docs)

**Next Phase**: Phase 1 - Data Model & Contracts

