#!/usr/bin/env bash
# Runs an epic's tasks unattended, one fresh Claude session per task, on one
# feature branch. Usage: scripts/epic-loop/run.sh docs/<epic>
# See README.md next to this file.
set -uo pipefail

[ $# -eq 1 ] || { echo "usage: $0 <epic-dir>" >&2; exit 2; }

HERE=$(cd "$(dirname "$0")" && pwd -P)
ROOT=$(git -C "$HERE" rev-parse --show-toplevel)
EPIC_DIR=$(cd "$1" && pwd -P) || exit 2
EPIC_REL=${EPIC_DIR#"$ROOT"/}
PROGRESS=$EPIC_DIR/progress.json
GATE=$EPIC_DIR/gate.sh
LOOP_DIR=$EPIC_DIR/loop
REPORT=$LOOP_DIR/morning-report.md
REVIEW=$LOOP_DIR/review.md
SETTINGS=$HERE/settings.json

MAX_ATTEMPTS=${MAX_ATTEMPTS:-2}
TASK_TIMEOUT_SECONDS=${TASK_TIMEOUT_SECONDS:-14400}
LIMIT_WAIT_SECONDS=${LIMIT_WAIT_SECONDS:-1800}
MAX_LIMIT_WAITS=${MAX_LIMIT_WAITS:-24}
CLAUDE_BIN=${CLAUDE_BIN:-$(command -v claude)}

[ -f "$PROGRESS" ] || { echo "missing $PROGRESS" >&2; exit 2; }
[ -x "$GATE" ] || { echo "missing or not executable: $GATE" >&2; exit 2; }
[ "$EPIC_REL" != "$EPIC_DIR" ] || { echo "epic must be inside the repo" >&2; exit 2; }

BRANCH=$(jq -er .branch "$PROGRESS") || exit 2
BASE=$(jq -er .base "$PROGRESS") || exit 2
case $BRANCH in feature/*) ;; *) echo "branch must start with feature/" >&2; exit 2 ;; esac
WORKTREE=$(dirname "$ROOT")/$(basename "$ROOT")-${BRANCH##*/}

# Pushing, pulling and fetching need a Secure Enclave key nobody is around to
# approve. This makes them fail at once instead of hanging on a prompt.
export GIT_SSH_COMMAND=/usr/bin/false GIT_TERMINAL_PROMPT=0

export JAVA_HOME=${JAVA_HOME_21:-$HOME/Library/Java/JavaVirtualMachines/ms-21.0.9/Contents/Home}
NODE_22=$(ls -d "$HOME"/.nvm/versions/node/v22* 2>/dev/null | tail -1)
[ -z "$NODE_22" ] || export PATH=$NODE_22/bin:$PATH
unset ESLINT_USE_FLAT_CONFIG

log() { echo "[$(date '+%H:%M:%S')] $*"; }
now() { date -u '+%Y-%m-%dT%H:%M:%SZ'; }

update_task() { # <id> <jq-expression>
  local tmp
  tmp=$(mktemp "$PROGRESS.XXXXXX")
  jq --arg id "$1" --arg now "$(now)" "(.tasks[] | select(.id == \$id)) |= ($2)" "$PROGRESS" >"$tmp" \
    && mv "$tmp" "$PROGRESS"
}

task_field() { jq -r --arg id "$1" ".tasks[] | select(.id == \$id) | .$2 // empty" "$PROGRESS"; }

write_report() {
  {
    echo "# Epic loop report"
    echo
    echo "Branch: \`$BRANCH\` (worktree \`$WORKTREE\`)"
    echo
    echo "Finished writing: $(date)"
    echo
    echo "| Task | Status | Attempts | Cost (USD) | Commit | Notes |"
    echo "|---|---|---|---|---|---|"
    jq -r '.tasks[] | "| \(.id) | \(.status) | \(.attempts // 0) | \(.costUsd // "-") | \(.commit // "-" | .[0:9]) | \(.notes // "" | gsub("\n"; " ")) |"' "$PROGRESS"
    echo
    echo "Session logs and gate output are in \`$EPIC_REL/loop/runs/\`."
    if [ -s "$REVIEW" ]; then
      echo
      echo "## Final review"
      echo
      cat "$REVIEW"
    fi
  } >"$REPORT"
}

setup_worktree() {
  git -C "$ROOT" rev-parse --verify --quiet "$BASE^{commit}" >/dev/null \
    || { echo "unknown base: $BASE" >&2; exit 2; }
  if ! git -C "$ROOT" worktree list --porcelain | grep -qxF "worktree $WORKTREE"; then
    if git -C "$ROOT" rev-parse --verify --quiet "refs/heads/$BRANCH" >/dev/null; then
      git -C "$ROOT" worktree add "$WORKTREE" "$BRANCH" || exit 2
    else
      git -C "$ROOT" worktree add -b "$BRANCH" "$WORKTREE" "$BASE" || exit 2
    fi
  fi
  # The epic folder is gitignored, so a new worktree doesn't have it. Share it,
  # and keep the symlink out of git: "docs/" in .gitignore doesn't match a link.
  local top=${EPIC_REL%%/*}
  [ -e "$WORKTREE/$top" ] || ln -s "$ROOT/$top" "$WORKTREE/$top"
  local exclude
  exclude=$(git -C "$ROOT" rev-parse --git-common-dir)
  case $exclude in /*) ;; *) exclude=$ROOT/$exclude ;; esac
  exclude=$exclude/info/exclude
  grep -qxF "/$top" "$exclude" 2>/dev/null || echo "/$top" >>"$exclude"
}

run_claude() { # <prompt> <log-file>; stops the session after the timeout
  (
    cd "$WORKTREE" && exec "$CLAUDE_BIN" -p "$1" \
      --permission-mode auto \
      --permission-prompts none \
      --settings "$SETTINGS" \
      --add-dir "$ROOT/${EPIC_REL%%/*}" \
      --output-format stream-json --verbose
  ) >"$2" 2>&1 </dev/null &
  local pid=$! rc
  ( sleep "$TASK_TIMEOUT_SECONDS" && kill -TERM "$pid" ) >/dev/null 2>&1 &
  local watchdog=$!
  wait "$pid"
  rc=$?
  kill "$watchdog" 2>/dev/null
  wait "$watchdog" 2>/dev/null
  return "$rc"
}

result_line() { grep '"type":"result"' "$1" | tail -1; }

hit_usage_limit() { # <log-file>
  local tail_of_log
  tail_of_log=$(result_line "$1"; tail -5 "$1")
  grep -qiE 'usage limit|session limit|weekly limit|rate.?limit|hit your .*limit|overloaded|"api_error_status":429' <<<"$tail_of_log"
}

session_failed() { # <exit-code> <log-file>
  [ "$1" -ne 0 ] || [ "$(result_line "$2" | jq -r '.is_error // false' 2>/dev/null)" != "false" ]
}

task_prompt() { # <id> <file> <attempt> <failure-file>
  cat <<PROMPT
You are one session in an unattended overnight loop. Nobody can answer questions or approve anything until the morning, so make every decision yourself.

Your task is task $1 of the epic in $EPIC_REL. Read $EPIC_REL/EPIC.md, then $EPIC_REL/$2, and follow them. Rules that differ from the epic because this run is unattended:

- Make your technical plan, but don't wait for approval. Record the choices a reviewer needs to see in the epic's Decisions section, then carry on.
- Stay on the branch $BRANCH. Don't create or change branches.
- Never run git push, git pull, git fetch or any other command that talks to a remote. They need an SSH key you don't have.
- Never merge, rebase, or touch main.
- First run git status and git log. An earlier session may have started this task, so continue from what is there.
- Commit your work with clear messages. When you finish, everything must be committed and the working tree clean.
- Don't edit $EPIC_REL/progress.json. The loop owns it. Do update the epic as your task's instructions say.
- Stay inside the task's scope. If something is unfinished or you had to accept a problem, record it under Known gaps.
- If you truly can't complete the task, leave your work committed, say why at the end of your final message, and stop. Don't weaken, skip or delete tests to get a pass.

The loop runs this check after you finish, so run it yourself first: bash $EPIC_REL/gate.sh
PROMPT
  if [ "$3" -gt 1 ] && [ -s "$4" ]; then
    printf '\nAttempt %s. The previous attempt failed the loop'"'"'s checks. Fix this first:\n\n' "$3"
    tail -c 6000 "$4"
  fi
}

verify_task() { # <id> <start-commit> <main-before> <failure-file>
  local head main
  head=$(git -C "$WORKTREE" rev-parse HEAD)
  main=$(git -C "$ROOT" rev-parse --verify --quiet refs/heads/main)
  : >"$4"
  if [ "$main" != "$3" ]; then
    echo "main moved during the run, from $3 to $main" >"$4"; return 2
  fi
  if [ "$(git -C "$WORKTREE" symbolic-ref --short HEAD 2>/dev/null)" != "$BRANCH" ]; then
    echo "The worktree is no longer on $BRANCH." >"$4"; return 1
  fi
  if [ "$head" = "$2" ]; then
    echo "The session made no commits." >"$4"; return 1
  fi
  if [ -n "$(git -C "$WORKTREE" status --porcelain)" ]; then
    { echo "The working tree isn't clean:"; git -C "$WORKTREE" status --short; } >"$4"; return 1
  fi
  return 0
}

run_task() { # <id>; returns 0 when done, 1 when blocked
  local id=$1 file start main_before attempt run_log gate_log failure rc waits=0 cost
  file=$(task_field "$id" file)
  start=$(task_field "$id" startCommit)
  [ -n "$start" ] || start=$(git -C "$WORKTREE" rev-parse HEAD)
  main_before=$(git -C "$ROOT" rev-parse --verify --quiet refs/heads/main)
  update_task "$id" ".status = \"in_progress\" | .startCommit = \"$start\" | .startedAt //= \$now"

  while :; do
    attempt=$(( $(task_field "$id" attempts | sed 's/^$/0/') + 1 ))
    if [ "$attempt" -gt "$MAX_ATTEMPTS" ]; then
      update_task "$id" '.status = "blocked" | .finishedAt = $now'
      return 1
    fi

    failure=$LOOP_DIR/runs/$id-failure.txt
    run_log=$LOOP_DIR/runs/$id-attempt-$attempt.jsonl
    log "task $id: attempt $attempt of $MAX_ATTEMPTS"
    run_claude "$(task_prompt "$id" "$file" "$attempt" "$failure")" "$run_log"
    rc=$?

    cost=$(result_line "$run_log" | jq -r '.total_cost_usd // empty' 2>/dev/null)
    [ -z "$cost" ] || update_task "$id" ".costUsd = ((.costUsd // 0) + $cost)"

    if session_failed "$rc" "$run_log" && hit_usage_limit "$run_log"; then
      waits=$((waits + 1))
      if [ "$waits" -gt "$MAX_LIMIT_WAITS" ]; then
        update_task "$id" '.status = "blocked" | .finishedAt = $now | .notes = "Stopped: usage limit did not clear."'
        return 1
      fi
      log "task $id: usage limit, waiting ${LIMIT_WAIT_SECONDS}s ($waits of $MAX_LIMIT_WAITS)"
      sleep "$LIMIT_WAIT_SECONDS"
      continue
    fi

    update_task "$id" ".attempts = $attempt"

    if session_failed "$rc" "$run_log"; then
      { echo "The session ended with an error (exit code $rc)."; tail -5 "$run_log"; } >"$failure"
      update_task "$id" '.notes = "Session ended with an error."'
      continue
    fi

    verify_task "$id" "$start" "$main_before" "$failure"
    rc=$?
    if [ "$rc" -eq 2 ]; then
      update_task "$id" ".status = \"blocked\" | .finishedAt = \$now | .notes = \"Stopped: main moved during the run.\""
      return 1
    elif [ "$rc" -ne 0 ]; then
      update_task "$id" '.notes = "Loop checks failed. See the failure file in loop/runs."'
      continue
    fi

    gate_log=$LOOP_DIR/runs/$id-gate-$attempt.log
    log "task $id: running the gate"
    ( cd "$WORKTREE" && GATE_BASE=$start bash "$GATE" ) >"$gate_log" 2>&1 </dev/null
    if [ $? -ne 0 ]; then
      { echo "The gate failed. Last lines of its output:"; tail -c 6000 "$gate_log"; } >"$failure"
      update_task "$id" '.notes = "Gate failed. See the gate log in loop/runs."'
      continue
    fi

    update_task "$id" ".status = \"done\" | .finishedAt = \$now | .notes = \"\" | .commit = \"$(git -C "$WORKTREE" rev-parse HEAD)\""
    log "task $id: done"
    return 0
  done
}

final_review() {
  [ ! -s "$REVIEW" ] || return 0
  log "running the final review"
  run_claude "Review all changes on this branch against $BASE (git diff $BASE...HEAD), as a reviewer who has not seen this work. Read $EPIC_REL/EPIC.md first. Look for bugs, behaviour changes, security problems and work the epic required but nobody did. Don't change, commit or push anything. Your final message must be the findings as a Markdown list, most serious first, or say that you found nothing." \
    "$LOOP_DIR/runs/final-review.jsonl"
  result_line "$LOOP_DIR/runs/final-review.jsonl" | jq -r '.result // empty' >"$REVIEW" 2>/dev/null
}

mkdir -p "$LOOP_DIR/runs"
if ! mkdir "$LOOP_DIR/.lock" 2>/dev/null; then
  echo "another loop is running (or remove $LOOP_DIR/.lock)" >&2
  exit 2
fi
trap 'rmdir "$LOOP_DIR/.lock"' EXIT
caffeinate -i -w $$ &

setup_worktree

while :; do
  id=$(jq -r '[.tasks[] | select(.status != "done")][0].id // empty' "$PROGRESS")
  if [ -z "$id" ]; then
    final_review
    write_report
    log "all tasks done. Report: $REPORT"
    exit 0
  fi
  if [ "$(task_field "$id" status)" = "blocked" ] || ! run_task "$id"; then
    write_report
    log "task $id is blocked, stopping. Report: $REPORT"
    exit 1
  fi
  write_report
done
