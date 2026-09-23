#!/usr/bin/env bash
# The Ralph loop: feed one unchanging prompt to a fresh agent, over and over,
# until the oracle goes green or the iteration budget runs out.
#
#   ./ralph.sh -n 12                          run it
#   ./ralph.sh -n 20 -m claude-sonnet-5 -b 5  pin the model, cap $5 per iteration
#   ./ralph.sh --reset                        back to the empty start state
#
# Each iteration is slow here (the oracle boots Spring and runs vitest, ~1 min),
# so budget accordingly and go and do something else.
set -euo pipefail
cd "$(dirname "$0")"

ITERATIONS=12
MODEL=""
MAX_BUDGET=""
ASSUME_YES=0
FORCE=0

while [ $# -gt 0 ]; do
  case "$1" in
    -n|--iterations) ITERATIONS="$2"; shift 2 ;;
    -m|--model)      MODEL="$2"; shift 2 ;;
    -b|--max-budget) MAX_BUDGET="$2"; shift 2 ;;
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
  # The lock records the hashes, so it must not move either: rewriting it
  # after editing a test would otherwise pass the check above.
  if ! git diff --quiet ralph-start -- .ralph/oracle.lock; then
    echo "Halting: .ralph/oracle.lock differs from ralph-start." >&2
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

# Pin the model and the spend when asked, and say what ran: a recording should show it.
AGENT_ARGS=(-p --dangerously-skip-permissions --output-format json)
[ -n "$MODEL" ] && AGENT_ARGS+=(--model "$MODEL")
[ -n "$MAX_BUDGET" ] && AGENT_ARGS+=(--max-budget-usd "$MAX_BUDGET")
MODEL_LABEL="${MODEL:-Claude Code default}"
BUDGET_LABEL="${MAX_BUDGET:+\$$MAX_BUDGET per iteration}"
RUN_CSV=".ralph/logs/run.csv"
[ -f "$RUN_CSV" ] || echo "started,iteration,seconds,passed,total,backend,frontend,commit,models,cost_usd,turns,tokens_in,tokens_out,subagents,outcome" > "$RUN_CSV"
RUN_STARTED="$(date +%Y-%m-%dT%H:%M:%S)"
RUN_T0=$SECONDS
TOTAL_COST=0

echo
echo "ralph: starting at $PREVIOUS/$TOTAL, budget $ITERATIONS iterations"
echo "model: $MODEL_LABEL, ${BUDGET_LABEL:-no spend cap}   ($(claude --version))"

for i in $(seq 1 "$ITERATIONS"); do
  rule "iteration $i of $ITERATIONS"
  check_oracle
  HEAD_BEFORE="$(git rev-parse HEAD)"
  LOG=".ralph/logs/iter-$(printf '%02d' "$i").log"

  # A fresh context window every iteration. The repo is the only memory.
  ITER_T0=$SECONDS
  echo "  agent working since $(date +%H:%M:%S) ..."
  claude "${AGENT_ARGS[@]}" < PROMPT.md > "$LOG" || true
  ELAPSED=$((SECONDS - ITER_T0))
  python tools/iteration_report.py "$LOG"
  AGENT="$(python tools/iteration_report.py "$LOG" --fields)"
  TOTAL_COST="$(python -c "import sys;print(f'{float(sys.argv[1]) + float(sys.argv[2] or 0):.2f}')" "$TOTAL_COST" "$(cut -d, -f2 <<< "$AGENT")")"

  check_oracle
  JSON="$(score)"
  PASSED="$(read_field passed "$JSON")"
  BACKEND="$(read_field backend_passed "$JSON")"
  FRONTEND="$(read_field frontend_passed "$JSON")"
  GREEN="$(read_field green "$JSON")"
  DELTA=$((PASSED - PREVIOUS))

  printf '  score %s/%s (%+d)   backend %s   frontend %s   %dm%02ds\n' \
    "$PASSED" "$TOTAL" "$DELTA" "$BACKEND" "$FRONTEND" $((ELAPSED / 60)) $((ELAPSED % 60))
  echo "$RUN_STARTED,$i,$ELAPSED,$PASSED,$TOTAL,$BACKEND,$FRONTEND,$(git rev-parse --short HEAD),$AGENT" >> "$RUN_CSV"
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
printf 'model %s, wall clock %02d:%02d:%02d, agent cost $%s\n' "$MODEL_LABEL" \
  $((WALL / 3600)) $((WALL % 3600 / 60)) $((WALL % 60)) "$TOTAL_COST"
python verify.py || true
echo
git --no-pager log --oneline ralph-start..HEAD
