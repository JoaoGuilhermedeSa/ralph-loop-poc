#!/usr/bin/env python3
"""Integrity check for the files Ralph is not allowed to touch.

The agent's exam, its instructions, the specification and the schema all live
outside its writable set. The *harness* checks them before and after every
iteration, because an agent that can quietly relax its own constraints will
eventually do so.

    python tools/oracle_lock.py write    # record hashes (the human runs this)
    python tools/oracle_lock.py check    # verify (the loop runs this)

This is a tripwire, not a sandbox. It catches drift and accidents; it is not a
defence against a determined agent. The real backstop is that every iteration
is one small commit you can read.
"""
from __future__ import annotations

import hashlib
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOCK = ROOT / ".ralph" / "oracle.lock"

PROTECTED = [
    "verify.py",
    "PROMPT.md",
    "AGENTS.md",
    "specs/*.md",
    "ralph.ps1",
    "ralph.sh",
    "tools/*.py",
    "backend/src/main/resources/db/migration/*.sql",
    "backend/src/test/java/ots/charcreate/acceptance/*.java",
    "backend/src/test/resources/acceptance/*",
    "frontend/tests/acceptance/*",
]


def digests() -> dict[str, str]:
    found: dict[str, str] = {}
    for pattern in PROTECTED:
        for path in sorted(ROOT.glob(pattern)):
            if path.is_file():
                rel = path.relative_to(ROOT).as_posix()
                found[rel] = hashlib.sha256(path.read_bytes()).hexdigest()
    return found


def main() -> int:
    mode = sys.argv[1] if len(sys.argv) > 1 else "check"
    current = digests()

    if mode == "write":
        LOCK.parent.mkdir(parents=True, exist_ok=True)
        LOCK.write_text("".join(f"{h}  {p}\n" for p, h in current.items()), encoding="utf-8")
        print(f"oracle locked: {len(current)} files")
        return 0

    if not LOCK.exists():
        print("oracle.lock missing - run: python tools/oracle_lock.py write", file=sys.stderr)
        return 1

    recorded = {
        line.split("  ", 1)[1]: line.split("  ", 1)[0]
        for line in LOCK.read_text(encoding="utf-8").splitlines()
        if line.strip()
    }

    problems = [f"MODIFIED  {p}" for p in recorded if p in current and current[p] != recorded[p]]
    problems += [f"DELETED   {p}" for p in recorded if p not in current]
    problems += [f"ADDED     {p}" for p in current if p not in recorded]

    if problems:
        print("ORACLE TAMPERED - halting the loop:", file=sys.stderr)
        for problem in problems:
            print(f"  {problem}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
