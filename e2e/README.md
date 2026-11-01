# End-to-end tests

Playwright tests for the frontend in `web/`. They pin what users can see and do.

The whole backend is mocked. The tests need no app, database or media service.

## Running

Run these from the `e2e/` folder, not `web/`:

```bash
cd e2e
nvm use
pnpm install
pnpm exec playwright install chromium
pnpm build:web
pnpm test
```

- `pnpm build:web` builds `web/` into `web/dist`. Rerun it after changing `web/`, because the tests run against that build.
- `pnpm test` starts `vite preview` on port 3100. Outside CI, a server already running on that port is reused.
- `pnpm test:ui` opens the Playwright UI, and `pnpm report` opens the last HTML report.

To test a server you started yourself, such as the build served by Spring:

```bash
BASE_URL=http://localhost:8080 pnpm test
```

Demo tests use the same server, because `applyDemoMode()` mocks `/api/v1/config`. Elsewhere the mock API answers that endpoint with a 401, which the app treats as non-demo.

The app calls the API and media service on its own origin. The mock adds no CORS headers.

## Layout

| Path | Purpose |
|---|---|
| `support/target.ts` | The only framework-aware file: server, thumbnails, demo mode |
| `support/mock-api.ts` | In-memory Java API, one instance per test |
| `support/contract.ts` | API types, each citing its Java source file |
| `support/factories.ts` | Fixture builders |
| `support/auth.ts` | Fake JWTs and stored sessions |
| `support/test.ts` | The extended `test`, with the `session` and `demoMode` options |
| `tests/` | Specs, which must stay framework-agnostic |

## Writing tests

- Import `test` and `expect` from `support/test`, never from `@playwright/test`, and never import from `web/src`.
- Tests start signed in. Use `test.use({ session: 'none' })` for a signed-out test.
- Set up data with `api.seedVideos()` and `api.seedDownloads()`. Force errors with `api.fail()`. Assert on calls with `api.requestsTo()`.
- Prefer `getByRole`, `getByLabel` and `getByText`. The only test ids are the three `queue-card-*` cards, whose counts have no accessible name.
- Use `gotoLogin()` for `/login`. It waits for the network to go idle, so the app has finished loading.
- Failed queries retry for about 7 seconds, so give error-state assertions a 15 second timeout.
- When the Java API changes, update `contract.ts` and `mock-api.ts` together.

Each test fails if the app calls an API path the mock doesn't handle, or requests any other host.

## What the specs require

The specs treat these as requirements of the app:

- the URLs, including `?progress=&page=` on downloads
- the `accessToken` and `refreshToken` keys in `localStorage`
- the visible text and accessible names
- `/media/{id}?token=...` for video requests
- `data-testid="queue-card-*"` on the queue cards

## Known gaps

**Token refresh can't work against the real API.** The client posts no body, its request interceptor overwrites the `Authorization` header it set, and it ignores the rotated refresh token. Java requires a JSON body with `refreshToken` and answers 400, so every 401 logs the user out.

The mock returns the same 400, so `a revoked session ends at login` pins today's behavior, and `a revoked session is refreshed and the request retried` is a `test.fixme` for the intended one. A fix must also exclude the refresh URL from the 401 interceptor. Otherwise a 401 from that endpoint retries forever.

Once refresh works, remove the `fixme` and delete the first test: with only the request body fixed, the `fixme` test passes and the other fails. The mock checks the body alone, so review the header and token rotation by hand.

**Deliberately not asserted**, so the suite doesn't lock in weak behavior:

- `/dashboard/video` shows "No videos" when the API fails
- downloads stays on "Loading..." until a poll succeeds
- an empty downloads table shows `Showing 1-0 of 0`
- "Open video" also copies the YouTube URL
- signed-in users can stay on `/login`

**A mocked backend can't cover:**

- contract drift between this mock and Java
- real JWT validation, revocation and role checks
- playback, seeking, and the media service's token check
- the Kafka and yt-dlp pipeline
- the real `/api/v1/config` response
- Traefik routing and Docker environment wiring
