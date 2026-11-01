# Tailkeep

A YouTube video archiver. `backend/` is one Java (Spring Boot) application, `web/` is the React UI and
`media/` is a Go media server. The backend is being refactored into a modular monolith with domain-driven
design and hexagonal architecture. The plan, the rules and the decisions made so far are in
[docs/refactor/EPIC.md](docs/refactor/EPIC.md) (the `docs/` folder is gitignored, so it only exists locally).
Read it before changing the backend, and keep its **Decisions** and **Known gaps** current.

## Backend modules (`backend/`, Gradle root)

| Module | Holds | May depend on |
|---|---|---|
| `mod-domain` | Models, use cases, ports. Plain Java: no Spring, Jakarta, Hibernate, Kafka or yt-dlp | nothing |
| `mod-adapter/database` | JPA entities, Spring Data repositories, mappers, port implementations | `mod-domain` |
| `mod-adapter/database-migration` | Flyway migrations only | nothing |
| `mod-adapter/web` | Controllers, request and response classes, exception handling | `mod-domain` |
| `mod-adapter/messaging`, `security`, `ytdlp` | Empty until tasks 04 to 06 (`ytdlp` has only the URL probe) | `mod-domain` |
| `mod-application` | Main class, config, full-stack tests, and the legacy code not yet migrated | everything |

`./gradlew verifyModuleDependencies` (part of `check`) fails when `mod-domain` has a dependency or an adapter
depends on anything but `mod-domain`. Adapters never depend on each other.

## Conventions

- **Packages:** `org.tailkeep.domain.<feature>` and `org.tailkeep.adapter.<adapter>.<feature>`. Features are
  singular (`video`, not `videos`: the root `.gitignore` has `**/videos`). `org.tailkeep.domain.shared` holds
  what features share. `org.tailkeep.api.*` and `org.tailkeep.worker.*` in `mod-application` are legacy.
- **Use cases:** an interface and an `Impl` with one public `execute()` (`GetVideo`, `GetVideoImpl`). The impl is
  annotated `@UseCase` (from `mod-domain`) with a plain constructor. `mod-application` scans for the annotation
  (`UseCaseConfig`), so the domain needs no Spring.
- **Ports:** interfaces in `mod-domain`, named by intent (`FindVideo`, `SaveVideo`, `PublishDownloadRequest`).
  They return domain types and `Optional`, never Spring types such as `Page`. Use `PageQuery`/`PageResult`.
  The use case decides what a missing result means.
- **Port implementations** are one `@Component` per feature in an adapter (`VideoDatabaseAdapter`), `@Transactional`,
  and they map entities to domain objects inside the transaction so no entity escapes.
- **Time:** use cases that need "now" take a `java.time.Clock` (the app provides it), never `LocalDateTime.now()`.
- **Transactions across ports:** a use case calls `RunInTransaction.execute(...)`. Do side effects like publishing
  after it returns.
- **Models** are records with Lombok `@Builder(toBuilder = true)`. Aggregates refer to each other by ID
  (`Video.channelId`), and use cases return a composed type (`VideoDetails`) when a response needs both.
- **Mappers** are hand-written, builder-based `final` classes with static methods (`VideoEntityMapper`,
  `VideoResponseMapper`). Each layer has its own classes (`Video`, `VideoEntity`, `VideoResponse`). No MapStruct
  in new code.
- Business rules live in the domain, never in an adapter. Component class names must be unique across modules,
  or the bean names collide.
- Behaviour must not change: REST paths and JSON, status codes, the schema, Kafka topic names and wire formats.
  The tests in `mod-application` pin them through HTTP, raw Kafka JSON and SQL, so seed test data with SQL and
  never through entities or repositories.

## Build and test

- Java 21. The default `java` is too new for Gradle 8.11:
  `export JAVA_HOME=~/Library/Java/JavaVirtualMachines/ms-21.0.9/Contents/Home`
- `cd backend && ./gradlew build` runs everything. The tests in `mod-application` use Testcontainers, so Docker
  must be running.
- Unit tests live in their module (domain tests use fakes and no Spring). Full-stack tests live in
  `mod-application` and go through HTTP.
- Ports 8080, 5000 and 7000 are taken locally, so run Spring on 8081. Node 22 through nvm for `web/`.
- `docker/compose.yml` collides locally on ports, container names and images: use a scratch override with its own
  `-p` project so the existing `tailkeep-java_*` volumes stay untouched.
