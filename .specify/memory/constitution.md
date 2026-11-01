<!--
Sync Impact Report:
Version: 0.0.0 → 1.0.0
Created: Initial constitution based on project analysis (README, build.gradle, package.json)

Principles defined:
- Modern Language & Framework Features (MANDATORY)
- Microservices Architecture & Bounded Contexts
- Event-Driven Communication
- Type Safety & Compile-Time Validation
- Integration Testing Focus
- Real-Time User Feedback
- Container-First Deployment

Templates requiring updates:
✅ plan-template.md - Constitution Check section will reference these principles
✅ spec-template.md - Already aligned with testable requirements
✅ tasks-template.md - Already aligned with independent testing approach

Follow-up TODOs: None - all placeholders filled

Rationale for MAJOR version (1.0.0):
- Initial constitution establishment
- Defines foundational architectural principles for the project
-->

# Tailkeep Constitution

## Core Principles

### I. Modern Language & Framework Features (MANDATORY)

**Non-Negotiable Rule**: All code MUST leverage modern language and framework features available in the project's current versions. No legacy patterns or outdated idioms are permitted unless absolutely required for compatibility.

**Specific Requirements**:
- **Java 21**: Use records, pattern matching, sealed classes, virtual threads, structured concurrency, and text blocks where applicable
- **Spring Boot 3.3+**: Use functional bean registration, native image support patterns, observability with Micrometer
- **React 18+**: Use hooks (never class components), concurrent features, automatic batching, transitions for non-urgent updates
- **Next.js 14+**: Use App Router (not Pages Router), Server Components by default, Server Actions for mutations, streaming SSR
- **TypeScript 5.4+**: Use strict mode, const assertions, satisfies operator, inferred types over explicit where clear

**Rationale**: The project uses up-to-date versions (Java 21, Spring Boot 3.3.5, Next.js 14, React 18). Staying current reduces technical debt, improves performance, and ensures security patches. Modern features provide better type safety, cleaner code, and improved developer experience.

**Anti-Patterns to Avoid**:
- Java: Excessive use of `@Autowired` field injection (use constructor injection), verbose getters/setters when records suffice, thread pools when virtual threads are better
- Spring: XML configuration, imperative bean definitions when functional is clearer
- React: Class components, useEffect for data fetching (use Server Components or React Query), prop drilling (use context or composition)
- TypeScript: `any` types, non-null assertions (`!`), type casts without validation

---

### II. Microservices Architecture & Bounded Contexts

**Non-Negotiable Rule**: The system is composed of four independent services (API, Worker, Web, Media), each with a clear bounded context. Services MUST NOT share databases or tightly couple. Each service owns its data and domain logic.

**Service Boundaries**:
- **API**: Authentication, user management, download job orchestration, metadata storage
- **Worker**: Video download execution, metadata fetching via yt-dlp, progress tracking
- **Web**: React UI, SSE client for real-time updates, user interactions
- **Media**: Static file serving (videos, thumbnails), optimized for high throughput

**Integration Rules**:
- Cross-service communication ONLY via Kafka message queues or HTTP APIs
- No shared database access between services
- Each service can have its own database schema/instance if needed
- Services MUST be independently deployable
- Breaking changes require versioned contracts

**Rationale**: Microservices enable independent scaling (Worker can scale for heavy downloads, Media for serving), technology diversity, fault isolation, and team autonomy. Current architecture already follows this pattern.

---

### III. Event-Driven Communication

**Non-Negotiable Rule**: Asynchronous operations MUST use Kafka for inter-service messaging. Event-driven patterns are preferred over synchronous HTTP for non-user-facing operations.

**Message Queue Requirements**:
- Use existing Kafka topics: `metadata-queue`, `metadata-results`, `download-queue`, `download-progress`
- All messages MUST be serializable JSON with versioned schemas
- Consumers MUST be idempotent (handle duplicate messages)
- Dead letter queues for failed message processing
- Structured logging for message flow tracing

**Real-Time Updates**:
- Use Server-Sent Events (SSE) for pushing progress updates from API to Web
- WebSockets only if bidirectional communication is required (currently not needed)

**Rationale**: Kafka decouples services, enables replay/audit trails, and handles backpressure. SSE provides efficient one-way push for progress updates without WebSocket complexity.

---

### IV. Type Safety & Compile-Time Validation

**Non-Negotiable Rule**: Leverage type systems to catch errors at compile time. Runtime type errors are considered bugs that should have been prevented.

**Java Requirements**:
- Use Lombok's `@NonNull` for required fields
- Use Optional<T> for nullable returns (never return null collections)
- Use MapStruct for type-safe DTO mapping (already in dependencies)
- Use Jakarta Validation annotations for request validation
- Records for immutable DTOs

**TypeScript Requirements**:
- Strict mode enabled (no implicit any, strict null checks)
- Use Zod for runtime validation + type inference (already in dependencies)
- Use React Hook Form with resolvers for form validation
- Discriminated unions for state machines

**Rationale**: Strong typing reduces bugs, improves IDE support, and serves as living documentation. Both Java 21 and TypeScript 5.4 have excellent type systems.

---

### V. Integration Testing Focus

**Non-Negotiable Rule**: All cross-service interactions and critical user journeys MUST have integration tests. Unit tests alone are insufficient for distributed systems.

**Required Test Coverage**:
- **API**: Integration tests using Testcontainers (PostgreSQL, Kafka, Redpanda)
- **Worker**: Integration tests for full download flows with real Kafka messaging
- **Cross-Service**: End-to-end tests for complete user journeys (submit download → receive progress → file available)
- **Contracts**: API contract tests to prevent breaking changes

**Testing Tools (Already in Dependencies)**:
- Testcontainers: Spin up real PostgreSQL, Kafka for integration tests
- Awaitility: Async assertion for event-driven flows
- AssertJ: Fluent assertions
- React Testing Library + MSW for frontend integration tests

**Rationale**: Microservices fail at integration boundaries. Testcontainers makes integration testing practical by providing real dependencies in CI/CD.

---

### VI. Real-Time User Feedback

**Non-Negotiable Rule**: Long-running operations (video downloads) MUST provide real-time progress feedback to users via SSE. No silent background processing for user-initiated actions.

**Requirements**:
- Download progress (percentage, speed, ETA) streamed via SSE
- Error states communicated immediately
- Optimistic UI updates with rollback on failure
- Toast notifications for state changes (using Sonner, already in dependencies)

**Rationale**: YouTube downloads can take minutes. Users need feedback to know the system is working. SSE is already implemented in the architecture diagram.

---

### VII. Container-First Deployment

**Non-Negotiable Rule**: All services MUST be containerized with Docker. Development environment MUST use Docker Compose. No "works on my machine" exceptions.

**Requirements**:
- Each service has a Dockerfile following multi-stage build patterns
- Docker Compose for local development (already exists: `docker/compose.dev.yml`)
- Traefik for routing (already in tech stack)
- Environment variable configuration (12-factor app principles)
- Health checks for all services

**Rationale**: Containers ensure consistency across dev/staging/prod. Docker Compose simplifies local development with all dependencies (PostgreSQL, Kafka, services) running together.

---

## Technology Constraints

### Approved Stack (NO SUBSTITUTIONS WITHOUT JUSTIFICATION)

**Backend**:
- Java 21 with Spring Boot 3.3.5
- Gradle 8.11.1 for build
- Lombok for boilerplate reduction
- PostgreSQL for persistent storage
- Flyway for database migrations
- Apache Kafka for messaging

**Frontend**:
- Next.js 14 (App Router only)
- React 18 (functional components + hooks only)
- TypeScript 5.4+ (strict mode)
- Tailwind CSS for styling
- Radix UI for accessible components
- TanStack Query for server state

**Infrastructure**:
- Docker + Docker Compose
- Traefik for reverse proxy
- PNPM for Node.js package management (NOT npm or yarn)

### Security Requirements

- JWT authentication with RSA signing (JJWT library, already in dependencies)
- Passwords hashed with BCrypt (Bouncy Castle, already in dependencies)
- HTTPS only in production (Traefik handles TLS)
- SQL injection prevention via JPA prepared statements
- XSS prevention via React automatic escaping + CSP headers
- CORS configured for known origins only

---

## Development Workflow

### Code Review Requirements

- All changes via pull requests
- At least one approval required
- CI must pass (tests, linting, build)
- No direct commits to main branch

### Quality Gates

**Before Merging**:
- All integration tests pass with Testcontainers
- TypeScript compilation with no errors (strict mode)
- ESLint/Prettier for frontend
- Gradle build success for backend
- Docker images build successfully

### Git Conventions

- Branch naming: `feature/<ticket>-<short-description>` or `bugfix/<ticket>-<short-description>`
- Commit messages: Conventional Commits format (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`)
- Squash merges to keep main branch history clean

---

## Governance

### Amendment Process

1. Propose amendment as pull request modifying this file
2. Discuss with team in PR comments
3. Require consensus (all active contributors)
4. Update version number (semantic versioning)
5. Propagate changes to dependent templates (plan-template.md, spec-template.md, tasks-template.md)

### Versioning Policy

- **MAJOR**: Removes or fundamentally changes a principle (breaking change to development process)
- **MINOR**: Adds new principle or expands existing guidance
- **PATCH**: Clarifications, typo fixes, non-semantic improvements

### Compliance Review

- Constitution check MUST be performed during planning phase (see plan-template.md)
- Violations require explicit justification in Complexity Tracking section
- Recurring violations trigger constitution amendment discussion

### Complexity Justification

If a feature violates principles (e.g., adds 5th service, uses legacy patterns):
1. Document in plan.md Complexity Tracking section
2. Explain why simpler alternatives are insufficient
3. Get explicit approval during code review
4. Consider whether violation indicates need to amend constitution

---

**Version**: 1.0.0 | **Ratified**: 2025-11-23 | **Last Amended**: 2025-11-23
