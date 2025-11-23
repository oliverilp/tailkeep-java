# Tasks: Channel Browsing

**Feature**: Channel Browsing  
**Branch**: `001-channel-browsing`  
**Input**: Design documents from `/specs/001-channel-browsing/`

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

**Tests**: No tests requested per constitution (standard CRUD operations, no critical integration points).

## Format: `- [ ] [ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend API**: `api/src/main/java/org/tailkeep/api/`
- All paths shown below are relative to repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and verification that prerequisites are met

- [ ] T001 Verify Channel and Video entities exist in api/src/main/java/org/tailkeep/api/model/
- [ ] T002 Verify EntityMapper exists in api/src/main/java/org/tailkeep/api/mapper/EntityMapper.java
- [ ] T003 Verify VideoRepository exists in api/src/main/java/org/tailkeep/api/repository/VideoRepository.java
- [ ] T004 Verify existing authentication configuration (JWT) is working

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core components that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 [P] Create ChannelRepository interface in api/src/main/java/org/tailkeep/api/repository/ChannelRepository.java
- [ ] T006 [P] Create ChannelDetailDto record in api/src/main/java/org/tailkeep/api/dto/ChannelDetailDto.java
- [ ] T007 [P] Create ChannelWithVideosDto class in api/src/main/java/org/tailkeep/api/dto/ChannelWithVideosDto.java
- [ ] T008 Update EntityMapper to add toDetailDto(Channel) mapping in api/src/main/java/org/tailkeep/api/mapper/EntityMapper.java
- [ ] T009 Add findByChannel(Channel, Pageable) method to VideoRepository in api/src/main/java/org/tailkeep/api/repository/VideoRepository.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Browse Available Channels (Priority: P1) 🎯 MVP

**Goal**: Users can retrieve a paginated list of all channels with basic information (name, video count), enabling channel discovery and navigation.

**Independent Test**: Make a GET request to `/api/v1/channels` and verify all existing channels are returned with their video counts in alphabetical order.

**Acceptance Criteria**:
- Returns paginated list of channels with video counts
- Channels sorted alphabetically (A-Z) by name
- Pagination works correctly (default: page 0, size 20)
- Requires JWT authentication (returns 401 without token)
- Empty list returned when no channels exist

### Implementation for User Story 1

- [ ] T010 [US1] Create ChannelService with getAllChannels method in api/src/main/java/org/tailkeep/api/service/ChannelService.java
- [ ] T011 [US1] Create ChannelController with GET /api/v1/channels endpoint in api/src/main/java/org/tailkeep/api/controller/ChannelController.java
- [ ] T012 [US1] Verify pagination parameters work correctly (page, size, sort) for channel list endpoint
- [ ] T013 [US1] Test channel list endpoint returns 401 without JWT token
- [ ] T014 [US1] Test channel list endpoint with empty database returns empty content array

**Checkpoint**: At this point, User Story 1 should be fully functional - users can browse all channels

---

## Phase 4: User Story 2 - View Channel Details and Videos (Priority: P2)

**Goal**: Users can view detailed information for a specific channel including all its videos (paginated), enabling focused content exploration.

**Independent Test**: Make a GET request to `/api/v1/channels/{id}` with a valid channel ID and verify channel details are returned with paginated video list sorted by newest first.

**Acceptance Criteria**:
- Returns channel details with paginated video list
- Videos sorted by newest first (descending createdAt)
- Video pagination works correctly (default: page 0, size 20)
- Returns HTTP 404 for non-existent channel ID
- Requires JWT authentication (returns 401 without token)
- Empty video list returned for channels with no videos

### Implementation for User Story 2

- [ ] T015 [US2] Add getChannelWithVideos method to ChannelService in api/src/main/java/org/tailkeep/api/service/ChannelService.java
- [ ] T016 [US2] Add GET /api/v1/channels/{id} endpoint to ChannelController in api/src/main/java/org/tailkeep/api/controller/ChannelController.java
- [ ] T017 [US2] Verify video pagination parameters work correctly (page, size) for channel detail endpoint
- [ ] T018 [US2] Test channel detail endpoint returns videos sorted by newest first
- [ ] T019 [US2] Test channel detail endpoint returns 404 for non-existent channel ID
- [ ] T020 [US2] Test channel detail endpoint returns 401 without JWT token
- [ ] T021 [US2] Test channel detail endpoint with channel that has no videos returns empty video list

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - users can browse channels and view channel details

---

## Phase 5: User Story 3 - Navigate Between Channels and Videos (Priority: P3)

**Goal**: Ensure seamless navigation between channel list, channel details, and individual videos through proper data relationships and response structure.

**Independent Test**: Verify that VideoDto includes channel information, enabling navigation from video to channel, and that all navigation paths return consistent data.

**Acceptance Criteria**:
- VideoDto includes nested ChannelDto for channel information
- Channel IDs in responses can be used to fetch channel details
- Video IDs in channel details can be used to fetch video details
- Navigation paths maintain data consistency

### Implementation for User Story 3

- [ ] T022 [US3] Verify VideoDto includes channel field (should already exist from existing VideoService)
- [ ] T023 [US3] Test end-to-end navigation: channel list → channel detail → video detail
- [ ] T024 [US3] Test reverse navigation: video detail → channel detail → channel list
- [ ] T025 [US3] Verify no circular reference issues in JSON serialization between Channel and Video DTOs

**Checkpoint**: All user stories should now be independently functional - complete navigation experience works

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements and validations that affect multiple user stories

- [ ] T026 [P] Run quickstart.md manual tests to validate all cURL examples work
- [ ] T027 [P] Verify performance meets success criteria (<2s for channel list, <3s for channel detail with 100 videos)
- [ ] T028 [P] Test with channels containing 0, 1, 50, and 100+ videos to verify pagination efficiency
- [ ] T029 [P] Verify lazy loading works correctly (no N+1 query issues for channel list)
- [ ] T030 [P] Test concurrent requests (simulate 50 simultaneous users browsing channels)
- [ ] T031 Review code for KISS compliance (method length <30 lines, file size <100 lines, no boolean params)
- [ ] T032 Verify MapStruct generates correct mapping implementation and DTO field accuracy (rebuild and check)
- [ ] T033 Review database query plans for channel_id foreign key performance (explain analyze on video queries)
- [ ] T034 Verify error rate monitoring meets 95% success rate target (SC-005)
- [ ] T035 Run application build to ensure no compilation errors (./gradlew build from api/)
- [ ] T036 Start application and verify endpoints are accessible (./gradlew bootRun from api/)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User stories can proceed in parallel (if staffed) after Phase 2
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Depends on Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Depends on Foundational (Phase 2) - Independent of US1 (can be tested standalone)
- **User Story 3 (P3)**: Depends on US1 and US2 completion - Validates integration

### Within Each User Story

**User Story 1**:
1. Create ChannelService (T010)
2. Create ChannelController (T011) - depends on T010
3. Validation tasks (T012-T014) - can run in parallel after T011

**User Story 2**:
1. Add service method (T015)
2. Add controller endpoint (T016) - depends on T015
3. Validation tasks (T017-T021) - can run in parallel after T016

**User Story 3**:
1. All tasks (T022-T025) can run in parallel once US1 and US2 are complete

### Parallel Opportunities

**Phase 2 (Foundational)**:
- T005, T006, T007 can run in parallel (different files)
- T008 and T009 depend on completion of T005-T007

**Phase 3 (User Story 1)**:
- T012, T013, T014 can run in parallel (different validation scenarios)

**Phase 4 (User Story 2)**:
- T017, T018, T019, T020, T021 can run in parallel (different validation scenarios)

**Phase 5 (User Story 3)**:
- T022, T023, T024, T025 can run in parallel (different navigation paths)

**Phase 6 (Polish)**:
- T026, T027, T028, T029, T030, T031, T032, T033, T034 can run in parallel (independent validations)

---

## Parallel Example: Foundational Phase

```bash
# Launch all DTO and Repository creation tasks together:
Task T005: "Create ChannelRepository interface in api/src/main/java/org/tailkeep/api/repository/ChannelRepository.java"
Task T006: "Create ChannelDetailDto record in api/src/main/java/org/tailkeep/api/dto/ChannelDetailDto.java"
Task T007: "Create ChannelWithVideosDto class in api/src/main/java/org/tailkeep/api/dto/ChannelWithVideosDto.java"
```

## Parallel Example: User Story 2 Validation

```bash
# Launch all validation tasks together after implementing the endpoint:
Task T017: "Verify video pagination parameters work correctly (page, size) for channel detail endpoint"
Task T018: "Test channel detail endpoint returns videos sorted by newest first"
Task T019: "Test channel detail endpoint returns 404 for non-existent channel ID"
Task T020: "Test channel detail endpoint returns 401 without JWT token"
Task T021: "Test channel detail endpoint with channel that has no videos returns empty video list"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T004)
2. Complete Phase 2: Foundational (T005-T009) - CRITICAL
3. Complete Phase 3: User Story 1 (T010-T014)
4. **STOP and VALIDATE**: Test channel list endpoint independently
5. Deploy/demo if ready

**Delivers**: Users can browse and discover all available channels with video counts

### Incremental Delivery

1. **Foundation**: Complete Setup + Foundational → Foundation ready
2. **MVP**: Add User Story 1 → Test independently → Deploy/Demo (Channel browsing works!)
3. **Enhanced**: Add User Story 2 → Test independently → Deploy/Demo (Channel details with videos!)
4. **Complete**: Add User Story 3 → Test independently → Deploy/Demo (Full navigation experience!)
5. **Polish**: Complete Phase 6 → Final validation → Production-ready

Each story adds value without breaking previous stories.

### Parallel Team Strategy

With multiple developers:

1. **Together**: Team completes Setup + Foundational (T001-T009)
2. **Once Foundational is done**:
   - Developer A: User Story 1 (T010-T014)
   - Developer B: User Story 2 (T015-T021) - can work in parallel
3. **After US1 & US2**: Either developer: User Story 3 (T022-T025)
4. **Together**: Team completes Polish phase (T026-T034)

---

## Task Summary

**Total Tasks**: 36

**Task Count by Phase**:
- Setup: 4 tasks
- Foundational: 5 tasks (blocking)
- User Story 1 (P1): 5 tasks
- User Story 2 (P2): 7 tasks
- User Story 3 (P3): 4 tasks
- Polish: 11 tasks

**Task Count by User Story**:
- User Story 1: 5 tasks (T010-T014)
- User Story 2: 7 tasks (T015-T021)
- User Story 3: 4 tasks (T022-T025)

**Parallel Opportunities**: 24 tasks marked [P] can run in parallel within their phase

**Independent Test Criteria**:
- US1: GET /api/v1/channels returns paginated channel list with video counts
- US2: GET /api/v1/channels/{id} returns channel details with paginated videos
- US3: Navigation paths between channel list, channel detail, and video detail work correctly

**Suggested MVP Scope**: User Story 1 only (T001-T014) - delivers channel browsing capability

---

## Format Validation

✅ All tasks follow checklist format: `- [ ] [ID] [P?] [Story?] Description`  
✅ All task IDs are sequential (T001-T036)  
✅ All user story tasks include story label ([US1], [US2], [US3])  
✅ All tasks include specific file paths or clear validation criteria  
✅ Parallelizable tasks marked with [P]  
✅ Setup and Foundational tasks have no story label  
✅ Polish tasks have no story label

---

## Notes

- No tests required per constitution (standard CRUD, no critical integration points)
- Backend focus only - no frontend tasks included
- Database schema already exists - no migrations needed
- Following existing VideoController/VideoService patterns
- All endpoints require JWT authentication (configured globally)
- Default pagination: page 0, size 20, max size 100
- Channel list sorted alphabetically (A-Z) by name
- Videos within channel sorted by newest first (descending createdAt)
- Performance targets: <2s for channel list, <3s for channel detail with 100 videos
- MapStruct handles DTO mapping automatically
- Spring Data JPA provides repository methods automatically
- [P] tasks = different files, no dependencies within phase
- [Story] label maps task to specific user story for traceability
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently

