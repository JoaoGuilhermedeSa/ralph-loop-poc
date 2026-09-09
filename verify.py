#!/usr/bin/env python3
"""The oracle for character creation.

Runs both acceptance suites and reports one score. Ralph may not edit this
file, the backend case table, or the frontend acceptance test.

    python verify.py              human-readable summary, exit 0 iff green
    python verify.py --json       machine-readable, for the loop runner
    python verify.py --backend    backend only (faster, while iterating)
    python verify.py --frontend   frontend only
"""
from __future__ import annotations

import json
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent
BACKEND = ROOT / "backend"
FRONTEND = ROOT / "frontend"
CASES = BACKEND / "src" / "test" / "resources" / "acceptance" / "cases.json"
EXPECTED = FRONTEND / "tests" / "acceptance" / "expected.json"

IS_WINDOWS = sys.platform == "win32"


def run(command: list[str], cwd: Path) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        command, cwd=cwd, capture_output=True, text=True, shell=IS_WINDOWS,
        encoding="utf-8", errors="replace",
    )


# --------------------------------------------------------------------------
# backend
# --------------------------------------------------------------------------

def backend_total() -> int:
    return len(json.loads(CASES.read_text(encoding="utf-8")))


def run_backend() -> dict:
    total = backend_total()
    reports = BACKEND / "target" / "surefire-reports"
    for stale in reports.glob("TEST-*.xml"):
        stale.unlink()

    result = run(["mvn", "-q", "-B", "test"], BACKEND)
    passed = failed = ran = 0
    for report in reports.glob("TEST-*.xml"):
        suite = ET.parse(report).getroot()
        ran += int(suite.get("tests", 0))
        failed += int(suite.get("failures", 0)) + int(suite.get("errors", 0))
    passed = ran - failed

    note = ""
    if ran == 0:
        note = "the suite did not run - the build is broken"
        passed = 0
    elif ran != total:
        note = f"TAMPERED: {ran} cases ran but cases.json declares {total}"
        passed = 0

    return {
        "passed": max(0, passed),
        "total": total,
        "ran": ran,
        "note": note,
        "log": (result.stdout + result.stderr)[-4000:],
    }


# --------------------------------------------------------------------------
# frontend
# --------------------------------------------------------------------------

def frontend_total() -> int:
    return int(json.loads(EXPECTED.read_text(encoding="utf-8"))["tests"])


def run_frontend() -> dict:
    total = frontend_total()
    if not (FRONTEND / "node_modules").is_dir():
        return {"passed": 0, "total": total, "ran": 0,
                "note": "node_modules missing - run: npm install --prefix frontend", "log": ""}

    out = FRONTEND / ".vitest-result.json"
    out.unlink(missing_ok=True)
    result = run(["npx", "vitest", "run", "--reporter=json", f"--outputFile={out.name}"], FRONTEND)

    if not out.exists():
        return {"passed": 0, "total": total, "ran": 0,
                "note": "the suite did not run - it could not be collected",
                "log": (result.stdout + result.stderr)[-4000:]}

    report = json.loads(out.read_text(encoding="utf-8"))
    ran = int(report.get("numTotalTests", 0))
    passed = int(report.get("numPassedTests", 0))

    note = ""
    if ran == 0:
        note = "the suite did not run - it could not be collected"
        passed = 0
    elif ran != total:
        note = f"TAMPERED: {ran} tests ran but expected.json declares {total}"
        passed = 0

    return {"passed": max(0, passed), "total": total, "ran": ran, "note": note,
            "log": (result.stdout + result.stderr)[-4000:]}


# --------------------------------------------------------------------------

def bar(passed: int, total: int, width: int = 30) -> str:
    filled = round(width * passed / total) if total else 0
    return f"[{'#' * filled}{'.' * (width - filled)}]"


def main() -> int:
    want_backend = "--frontend" not in sys.argv
    want_frontend = "--backend" not in sys.argv

    backend = run_backend() if want_backend else {"passed": 0, "total": 0, "note": "", "log": ""}
    frontend = run_frontend() if want_frontend else {"passed": 0, "total": 0, "note": "", "log": ""}

    passed = backend["passed"] + frontend["passed"]
    total = backend["total"] + frontend["total"]
    green = passed == total and total > 0

    if "--json" in sys.argv:
        print(json.dumps({
            "backend_passed": backend["passed"], "backend_total": backend["total"],
            "frontend_passed": frontend["passed"], "frontend_total": frontend["total"],
            "passed": passed, "total": total, "green": green,
            "notes": [n for n in (backend.get("note"), frontend.get("note")) if n],
        }))
        return 0 if green else 1

    if want_backend:
        print(f"  backend   {bar(backend['passed'], backend['total'])} "
              f"{backend['passed']}/{backend['total']}")
        if backend["note"]:
            print(f"            {backend['note']}")
    if want_frontend:
        print(f"  frontend  {bar(frontend['passed'], frontend['total'])} "
              f"{frontend['passed']}/{frontend['total']}")
        if frontend["note"]:
            print(f"            {frontend['note']}")

    print(f"  {'-' * 46}")
    print(f"  total     {bar(passed, total)} {passed}/{total}   => {'GREEN' if green else 'RED'}")

    if not green and "--quiet" not in sys.argv:
        for name, section in (("backend", backend), ("frontend", frontend)):
            log = section.get("log", "")
            if log and section["passed"] < section["total"]:
                print(f"\n  --- last of the {name} log " + "-" * 30)
                print("\n".join(f"  {line}" for line in log.strip().splitlines()[-12:]))

    return 0 if green else 1


if __name__ == "__main__":
    raise SystemExit(main())
