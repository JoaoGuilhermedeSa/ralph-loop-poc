#!/usr/bin/env bash
# The Ralph loop: feed one unchanging prompt to a fresh agent, over and over,
# until the oracle goes green or the iteration budget runs out.
#
#   ./ralph.sh -n 12                     run it
#   ./ralph.sh -n 12 -m claude-sonnet-5  pin the model
#   ./ralph.sh --reset                   back to the empty start state
#
# Each iteration is slow here (the oracle boots Spring and runs vitest, ~1 min),
# so budget accordingly and go and do something else.
set -euo pipefail
cd "$(dirname "$0")"

ITERATIONS=12
MODEL=""
ASSUME_YES=0
FORCE=0

while [ $# -gt 0 ]; do
  case "$1" in
    -n|--iterations) ITERATIONS="$2"; shift 2 ;;
    -m|--model)      MODEL="$2"; shift 2 ;;
    -y|--yes)        ASSUME_YES=1; shift ;;
    -f|--force)      FORCE=1; shift ;;
    --reset)
      if [ -n "$(git status --porcelain)" ] && [ "$FORCE" -eq 0 ]; then
        echo "Working tree is dirty. --reset would discard:" >&2
        git status --short >&2
        echo "Commit them first, or re-run with --reset --force." >&2
        exit 1
      fi
      git reset --hard ralph-start --quiet
      git clean -fdxq backend/src/main/java frontend/src \
                      backend/src/test/java/ots/charcreate/unit frontend/tests/unit 2>/dev/null || true
      rm -f .ralph/logs/*.log
      echo "Reset to the start state."
      exec python verify.py ;;
    *) echo "unknown option: $1" >&2; exit 64 ;;
  esac
done

rule() { printf '\n-- %s ' "$1"; printf -- '-%.0s' $(seq $((68 - ${#1}))); printf '\n'; }
score() { python verify.py --json || true; }
read_field() { python -c "import json,sys;print(json.loads(sys.argv[1])['$1'])" "$2"; }

check_oracle() {
  if ! python tools/oracle_lock.py check; then
    echo "Halting: the files Ralph may not edit have changed." >&2
    exit 1
  fi
}

if [ "$ASSUME_YES" -eq 0 ]; then
  echo "This runs the agent unattended with --dangerously-skip-permissions."
  echo "It will edit files and commit on its own."
  read -r -p "Continue? [y/N] " answer
  [ "$answer" = "y" ] || exit 0
fi

mkdir -p .ralph/logs

JSON="$(score)"
PREVIOUS="$(read_field passed "$JSON")"
TOTAL="$(read_field total "$JSON")"
STALLS=0

# Pin the model when asked, and say which one ran: a recording should show it.
MODEL_ARGS=()
[ -n "$MODEL" ] && MODEL_ARGS=(--model "$MODEL")
MODEL_LABEL="${MODEL:-Claude Code default}"
RUN_CSV=".ralph/logs/run.csv"
[ -f "$RUN_CSV" ] || echo "started,iteration,model,seconds,passed,total,backend,frontend,commit" > "$RUN_CSV"
RUN_STARTED="$(date +%Y-%m-%dT%H:%M:%S)"
RUN_T0=$SECONDS

echo
echo "ralph: starting at $PREVIOUS/$TOTAL, budget $ITERATIONS iterations"
echo "model: $MODEL_LABEL   ($(claude --version))"

for i in $(seq 1 "$ITERATIONS"); do
  rule "iteration $i of $ITERATIONS"
  check_oracle
  HEAD_BEFORE="$(git rev-parse HEAD)"
  LOG=".ralph/logs/iter-$(printf '%02d' "$i").log"

  # A fresh context window every iteration. The repo is the only memory.
  ITER_T0=$SECONDS
  claude -p --dangerously-skip-permissions ${MODEL_ARGS[@]+"${MODEL_ARGS[@]}"} < PROMPT.md 2>&1 | tee "$LOG"
  ELAPSED=$((SECONDS - ITER_T0))

  check_oracle
  JSON="$(score)"
  PASSED="$(read_field passed "$JSON")"
  BACKEND="$(read_field backend_passed "$JSON")"
  FRONTEND="$(read_field frontend_passed "$JSON")"
  GREEN="$(read_field green "$JSON")"
  DELTA=$((PASSED - PREVIOUS))

  printf '  score %s/%s (%+d)   backend %s   frontend %s   %dm%02ds\n' \
    "$PASSED" "$TOTAL" "$DELTA" "$BACKEND" "$FRONTEND" $((ELAPSED / 60)) $((ELAPSED % 60))
  echo "$RUN_STARTED,$i,$MODEL_LABEL,$ELAPSED,$PASSED,$TOTAL,$BACKEND,$FRONTEND,$(git rev-parse --short HEAD)" >> "$RUN_CSV"
  [ "$DELTA" -lt 0 ] && echo "  regression; the next iteration must fix it before taking new work."

  if [ "$(git rev-parse HEAD)" = "$HEAD_BEFORE" ]; then
    STALLS=$((STALLS + 1))
    echo "  no commit this iteration (stall $STALLS of 3)"
    if [ "$STALLS" -ge 3 ]; then
      echo "Halting: three iterations with nothing committed. Go read fix_plan.md." >&2
      break
    fi
  else
    STALLS=0
  fi

  PREVIOUS="$PASSED"
  REMAINING="$(grep -c '^\s*- \[ \]' fix_plan.md || true)"
  if [ "$GREEN" = "True" ] && [ "$REMAINING" -eq 0 ]; then
    rule "done"
    echo "Green, and the backlog is empty, after $i iterations."
    break
  fi
done

rule "summary"
WALL=$((SECONDS - RUN_T0))
printf 'model %s, wall clock %02d:%02d:%02d\n' "$MODEL_LABEL" $((WALL / 3600)) $((WALL % 3600 / 60)) $((WALL % 60))
python verify.py || true
echo
git --no-pager log --oneline ralph-start..HEAD
