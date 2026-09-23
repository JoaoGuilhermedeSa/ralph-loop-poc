#!/usr/bin/env python3
"""Summarise one iteration from the agent's `--output-format json` log.

The loop runner calls this after every iteration, so the console shows what
the agent said and `.ralph/logs/run.csv` records how much work it took.

Cost is Claude Code's own estimate at API list price (`costBasis: list`).
Under a Claude subscription nothing is billed per token, so read it as "what
this would cost on the API", not as money spent.

    python tools/iteration_report.py LOG            the agent's final message + one stats line
    python tools/iteration_report.py LOG --fields   models,est_cost_usd,turns,tokens_in,tokens_out,subagents,outcome

A log that is not JSON (the CLI crashed, or was killed) is printed as-is and
reported with empty fields and outcome `no-json`, so the loop keeps going and
the stall detector decides what happens next.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path


def load(path: Path) -> tuple[dict | None, str]:
    raw = path.read_text(encoding="utf-8-sig", errors="replace") if path.exists() else ""
    try:
        return json.loads(raw), raw
    except json.JSONDecodeError:
        return None, raw


def fields(data: dict) -> dict[str, str]:
    usage = data.get("modelUsage") or {}
    tokens_in = sum(
        m.get("inputTokens", 0) + m.get("cacheReadInputTokens", 0) + m.get("cacheCreationInputTokens", 0)
        for m in usage.values()
    )
    tokens_out = sum(m.get("outputTokens", 0) for m in usage.values())
    return {
        # ';' keeps the CSV to one column when subagents ran on another model.
        "models": ";".join(usage) or "unknown",
        "est_cost_usd": f"{data.get('total_cost_usd', 0):.2f}",
        "turns": str(data.get("num_turns", "")),
        "tokens_in": str(tokens_in),
        "tokens_out": str(tokens_out),
        "subagents": str((data.get("subagent_stats") or {}).get("spawned", 0)),
        "outcome": data.get("subtype", "unknown"),
    }


def main() -> int:
    if len(sys.argv) < 2:
        print(__doc__, file=sys.stderr)
        return 64
    data, raw = load(Path(sys.argv[1]))

    if "--fields" in sys.argv:
        if data is None:
            print(",,,,,,no-json")
        else:
            print(",".join(fields(data).values()))
        return 0

    if data is None:
        print(raw.rstrip() or "(the agent produced no output)")
        print("  agent: output was not JSON - see the log")
        return 0

    print(str(data.get("result", "")).rstrip())
    f = fields(data)
    print(
        f"  agent: {f['models']}   ~${f['est_cost_usd']} at API list price   {f['turns']} turns   "
        f"{int(f['tokens_in']):,} tokens in / {int(f['tokens_out']):,} out   "
        f"{f['subagents']} subagents   {f['outcome']}"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
