# Tailkeep Constitution

<!--
Sync Impact Report:
- Version change: [new constitution] → 1.0.0
- Rationale: Initial constitution based on project README and KISS principles
- Modified principles: N/A (initial version)
- Added sections: All sections (initial)
- Removed sections: None
- Templates requiring updates:
  ✅ plan-template.md - reviewed, aligned with constitution principles
  ✅ spec-template.md - reviewed, aligned with user story requirements
  ✅ tasks-template.md - reviewed, aligned with testing discipline (optional tests)
- Follow-up TODOs: None
-->

## Core Principles

### I. KISS - Keep It Simple, Stupid (NON-NEGOTIABLE)

**MUST Follow:**

- Code is primarily written for other developers to read, not machines
- Code MUST be readable and understandable **without relying on comments**
- **Avoid comments** unless absolutely necessary - explain code in conversations/PRs instead
- **Single Responsibility Principle (SRP)**: A method/class/module does only one thing
- **Avoid abbreviations and encoding** in names - use clear, descriptive names
- **Method signatures**: Maximum 3-4 parameters
- **No boolean parameters** - indicates SRP violation; split into separate methods
- **File length**: Preferably <100 lines (maximum 500 lines) - longer files indicate design smell
- **Prefer simple code over abstraction**: Favor readability over marginal DRY reductions
- **Avoid unnecessary "magic"**: Don't reduce line count at the cost of clarity
- **Prefer `??` operator** over `||` for null coalescing in TypeScript/JavaScript

**Rationale**: Code is read far more than written. Clarity reduces bugs, speeds onboarding, and improves maintainability. Complex abstractions harm more than they help in typical scenarios.

### II. Modern Framework & Language Features

**MUST Use:**

- **Java 21** features: Records, Pattern Matching, Sealed Classes, Virtual Threads where appropriate
- **Spring Boot 3.3.5** modern patterns: Constructor injection, functional endpoints, WebFlux where beneficial
- **Lombok** annotations to reduce boilerplate: `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`
- **TypeScript 5.4.5** features: Type inference, strict null checks, const assertions, satisfies operator
- **React 18** features: Hooks (no class components), Suspense, Server Components (Next.js 14)
- **Next.js 14** features: App Router, Server Actions, streaming, partial prerendering
- **Modern JavaScript/TypeScript**: Async/await over callbacks, optional chaining, nullish coalescing

**Avoid:**

- Legacy patterns (Java 8 style, class components in React, outdated Spring patterns)
- Verbose code when modern language features provide cleaner alternatives
- Unnecessary polyfills or compatibility layers for supported environments

**Rationale**: Using modern features leverages framework/language improvements for cleaner, more maintainable code. Our dependencies are up-to-date, so we should use them fully.

### III. Readable Code Over Comments

**MUST Follow:**

- Self-documenting code through clear naming: `calculateTotalPrice()` not `calc()` or `doStuff()`
- Extract complex logic into well-named methods rather than commenting inline
- Use type systems (Java generics, TypeScript types) to express intent
- Domain-driven naming: `User`, `Video`, `DownloadProgress` not `UserObj`, `VideoData`
- Package/module structure reflects domain concepts, not technical layers alone

**Allowed Comments:**

- Public API documentation (JavaDoc, JSDoc) for library boundaries
- Complex algorithm explanations when unavoidable (cite sources/papers)
- TODO/FIXME with ticket numbers and context
- Business rule rationale when non-obvious (e.g., "Per GDPR Article 17...")

**Prohibited:**

- Comments that restate what code already says (`// increment counter`)
- Commented-out code (use version control)
- Misleading or outdated comments

**Rationale**: Comments become stale and misleading. Code that reads like prose is self-maintaining.

### IV. Microservices Architecture

**System Structure (MUST Maintain):**

- **API** (Spring Boot): Business logic, JWT authentication, REST endpoints, SSE for real-time updates
- **Worker** (Spring Boot): Background video downloading, yt-dlp integration, queue processing
- **Web** (Next.js): React UI, server-side rendering, client-side interactivity
- **Media** (Go): Static file serving for videos and images

**Communication Patterns:**

- **Kafka Topics**: `metadata-queue`, `metadata-results`, `download-queue`, `download-progress`
- **REST APIs**: API ↔ Web communication
- **Server-Sent Events (SSE)**: Real-time progress updates from API to Web
- **Message Queue**: Async communication between API and Worker

**Data Storage:**

- **PostgreSQL**: Primary data store (users, videos, channels, jobs, download progress)
- **Flyway**: Database migrations (versioned, reproducible)
- **File System**: Video and image files served by Media service

**MUST Follow:**

- Service boundaries remain clean - no direct database access across services
- API owns business logic and data consistency
- Worker is stateless and idempotent for queue processing
- Web contains no business logic - delegates to API
- Media is a simple static file server with no business logic

**Rationale**: Clear service boundaries enable independent scaling, deployment, and development.

### V. Testing Discipline (Pragmatic, Not Dogmatic)

**Default Approach:**

- Tests exist in the repository but are **NOT mandatory by default**
- Test-Driven Development (TDD) is **NOT required** unless explicitly requested
- Focus on critical paths, integration points, and bug-prone areas
- Prefer integration tests over unit tests for business value

**MUST Test:**

- Kafka message contract changes (producer/consumer compatibility)
- Authentication and authorization logic
- Data migrations (Flyway scripts)
- Critical business flows (video download workflow, user authentication)

**Testing Stack:**

- **Java/Spring**: JUnit 5, Mockito, Testcontainers, Spring Test, Awaitility
- **TypeScript/React**: Jest (implied by Next.js setup), React Testing Library if needed

**When Tests ARE Required (User Explicitly Requests):**

- Follow Test-First approach: Write test → Verify it fails → Implement → Verify it passes
- Organize tests by user story for traceability
- Integration tests validate real component interactions (DB, Kafka, HTTP)
- Contract tests verify API/queue message schemas

**Rationale**: Pragmatic testing focuses effort where it provides most value. Not all code needs tests, but critical paths and integration boundaries do. TDD is valuable when requested but not enforced by default.

## Technology Stack

**Backend (Java):**

- Java 21 with preview features enabled where beneficial
- Spring Boot 3.3.5 (Web, Security, Data JPA, Kafka)
- Lombok for boilerplate reduction
- MapStruct for DTO mapping
- PostgreSQL with Flyway migrations
- Apache Kafka for async messaging
- JWT (jjwt) for authentication

**Frontend (TypeScript):**

- Next.js 14.2.3 (App Router)
- React 18
- TypeScript 5.4.5 (strict mode)
- TailwindCSS for styling
- Radix UI for components
- TanStack Query for data fetching
- Zod for schema validation

**Infrastructure:**

- Docker for containerization
- Traefik for reverse proxy
- PNPM for package management (frontend)
- Gradle 8.11.1 for build (backend)

## Development Workflow

**Code Organization:**

- Java: Package by feature/domain, not by layer (`org.tailkeep.api.video.*` not `org.tailkeep.api.controllers.*`)
- TypeScript: Organize by feature (`app/dashboard/downloads/*`) with shared libs in `lib/`
- Keep related code together for easier navigation and understanding

**Code Review Standards:**

- Verify KISS compliance: Can a new developer understand this without explanation?
- Check for SRP violations: Does this class/method do more than one thing?
- Ensure modern features used where appropriate
- No commented-out code
- Methods <30 lines ideally, <100 lines maximum
- Files <500 lines

**Complexity Justification Required For:**

- Methods with >4 parameters
- Files >500 lines
- Boolean method parameters
- Deep inheritance hierarchies (prefer composition)
- Heavy abstraction layers (repositories, factories, builders) without clear need

**Deployment:**

- Docker Compose for local development
- Environment-specific configs: `application-dev.properties`, `application-prod.properties`
- Feature branches follow pattern: `###-feature-name`

## Governance

**Constitution Authority:**

This constitution supersedes all other practices and guidelines. When in doubt, refer to these principles.

**Amendment Process:**

1. Propose changes with rationale in discussion/PR
2. Update constitution version following semantic versioning:
   - MAJOR: Backward incompatible principle changes, removals
   - MINOR: New principles added, material expansions
   - PATCH: Clarifications, wording improvements, typo fixes
3. Update dependent templates in `.specify/templates/` for consistency
4. Document changes in Sync Impact Report (HTML comment at top of this file)

**Compliance Review:**

- All PRs must verify constitution compliance, especially KISS principles
- Use `.specify/templates/plan-template.md` Constitution Check section for feature planning
- Reject unnecessary complexity - simplicity is a feature, not a compromise
- Tests are optional by default - only include when explicitly requested or for critical paths

**Version**: 1.0.0 | **Ratified**: 2025-11-23 | **Last Amended**: 2025-11-23
