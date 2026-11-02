# Epic loop

Runs an epic's tasks overnight, one fresh Claude session per task, on one `feature/<name>` branch.
You review the finished branch, push it and open the PR yourself. Nothing here pushes or merges.

## Set up an epic

The epic folder needs:

- `EPIC.md` and one file per task
- `progress.json`, which tracks the tasks. Only this script writes it while running
- `gate.sh`, an executable check that exits non-zero on failure. `GATE_BASE` holds the commit the task started from

```json
{
  "branch": "feature/my-epic",
  "base": "main",
  "tasks": [
    { "id": "01", "file": "task-01-first.md", "status": "todo", "attempts": 0 }
  ]
}
```

Statuses are `todo`, `in_progress`, `done` and `blocked`.

## Run

Keep Docker running if the gate needs it, then:

```bash
scripts/epic-loop/run.sh docs/refactor
```

Work happens in a new worktree next to the repo, `<repo>-<name>`, so your own checkout stays free. The script
keeps the Mac awake and can be run again after any stop: it continues from `progress.json`.

## What happens

1. The first task that isn't done gets a fresh session in auto mode.
2. The script checks the session committed, stayed on the branch and left a clean tree, then runs `gate.sh`.
3. A failed check gets one retry with the failure output. A second failure blocks the task and stops the loop.
4. Usage limits don't count as attempts. The script waits and starts the task again.
5. After the last task, a fresh session reviews the whole branch.

Read `<epic>/loop/morning-report.md` in the morning. Logs are in `<epic>/loop/runs/`.

To retry a blocked task, fix the cause, then set its `status` to `todo` and `attempts` to `0`.

## Settings

Environment variables: `MAX_ATTEMPTS` (2), `TASK_TIMEOUT_SECONDS` (14400), `LIMIT_WAIT_SECONDS` (1800),
`MAX_LIMIT_WAITS` (24).

## Git safety

`settings.json` denies push, pull, fetch, merge, rebase and branch switching. `GIT_SSH_COMMAND` also makes any
SSH remote call fail at once, so nothing can hang waiting for the Secure Enclave key. The script stops if `main`
moves during a task, so don't commit to `main` while it runs.
