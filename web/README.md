The Tailkeep web UI: a client-only React app built with [Vite](https://vite.dev/) and [TanStack Router](https://tanstack.com/router). In production the Spring API serves the build.

## Getting Started

Use Node 22 (`nvm use`), then start the development server:

```bash
pnpm install
pnpm dev
```

Open [http://localhost:3000](http://localhost:3000). The dev server proxies `/api` to `http://localhost:8080` and `/media` to `http://localhost:5000`. Set `API_URL` or `MEDIA_URL` to use other addresses.

## Scripts

| Command | Purpose |
|---|---|
| `pnpm dev` | Start the dev server with hot reload |
| `pnpm build` | Typecheck and build into `dist/` |
| `pnpm preview` | Serve the build locally |
| `pnpm typecheck` | Run TypeScript |
| `pnpm lint` | Run ESLint |

## Layout

- `src/routes/` holds the file-based routes. The Vite plugin regenerates `src/routeTree.gen.ts` from them, and that file is committed because `pnpm build` typechecks before Vite runs.
- `src/features/` holds the larger page components.

The behavior tests live in `../e2e`.
