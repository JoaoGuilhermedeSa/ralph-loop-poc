# Character creation for an Open Tibia server — built by a Ralph loop

A real feature, built the way the talk describes: a spec, an oracle the agent
cannot edit, a backlog, and a `while` loop.

The talk itself, and a 30-second toy version of the same harness, live in the
companion repo **`ralph-loop`**. This one is where the technique meets work
that actually has to ship.

    python verify.py        # 0/68 right now - nothing is implemented yet
    ./ralph.sh -n 20 -m claude-sonnet-5
    # or: .\ralph.ps1 -Iterations 20 -Model claude-sonnet-5

Then read what it did:

    git log --oneline ralph-start..HEAD
    cat .ralph/journal.md
    cat .ralph/logs/run.csv     # per-iteration score, time, model, tokens, est. cost

## What gets built

`POST /api/characters` — pick a name, a vocation, a sex and a town; get a
character row with the right starting stats. Java 21 + Spring Boot 4 + JPA +
Flyway behind it, React 19 + Vite in front, MySQL 8 for dev and in-memory H2
for the tests.

Character creation is a good loop target for one reason: **the oracle is
cheap.** Every rule is a fact, not a taste call — a Knight starting at level 8
has exactly 255 hitpoints, 35 mana and 575 capacity, and `2 ^ 3 ^ 2`-style
arguments about it do not happen.

## The score

68 acceptance cases, and none of them are the agent's to change.

| suite | cases | what it pins |
|---|---:|---|
| backend | 56 | starting stats per vocation, outfits per sex, spawn positions, name rules and their order, the full error table, uniqueness, the character limit |
| frontend | 12 | the form contract, client validation, submit, result panel, server and network error handling |

The backend cases are **generated** from the formulas in `specs/01-domain.md`
by `tools/gen_cases.py`, so the expected numbers are derived rather than
transcribed. A typo in the table is not possible; only a wrong formula is, and
that shows up immediately as an unsatisfiable case.

## Anatomy

| path | role | owner |
|---|---|---|
| `PROMPT.md` | the one prompt, fed verbatim every iteration | human |
| `AGENTS.md` | hard constraints that survive a fresh context | human |
| `specs/` | source of truth for behaviour | human |
| `fix_plan.md` | the backlog — **edit this to steer the run** | both |
| `.ralph/journal.md` | one line per iteration, for its successor | agent |
| `verify.py` | the oracle: runs both suites, prints one score | locked |
| `tools/iteration_report.py` | turns the agent's JSON output into the console summary and `run.csv` | locked |
| `tools/oracle_lock.py` | tripwire over the 19 files Ralph may not touch | locked |
| `backend/`, `frontend/` | where the agent actually writes | agent |

`db/migration/V1__init.sql` is locked too. `spring.jpa.hibernate.ddl-auto` is
`validate` in every profile, so an entity that disagrees with the schema fails
the context on startup rather than silently reshaping the database.

## Guardrails

- **Bounded budget** — `-n` / `-Iterations`, never `while true` unattended.
- **Stall detector** — three iterations with no commit and the loop halts.
- **Oracle tripwire** — the 19 protected files (oracle, specs, schema,
  prompt, house rules, the runners themselves) are hashed before and after
  every iteration; any change halts the run. The lock file holding those
  hashes must also match `ralph-start`, so re-recording it does not help.
- **Spend cap, API keys only** — `-MaxBudgetUsd` / `-b` passes
  `--max-budget-usd` to each iteration. Off by default, and usually left off:
  `claude -p` normally runs on your Claude Code login, where nothing is billed
  per token and the subscription's usage limits are the real ceiling. The cap
  then only cuts an iteration short, often mid-edit and uncommitted, which
  hands the next iteration a dirty tree.
- **Score guard** — the total may not fall; a regression becomes the next
  iteration's only job.
- **Dirty-tree guard** — `--reset` refuses to discard uncommitted work.

Smoke detectors, not a sandbox. The real backstop is one small commit per
iteration that a human can read.

## Recording a run

The runner is built to be screen-recorded and sped up:

- the header names the model (pass `-Model` / `-m` to pin it; otherwise it
  says "Claude Code default") and the Claude Code version;
- each iteration prints the agent's closing message, then one line with the
  model(s) used, estimated cost, turns, tokens and subagents, then the score and how
  long the iteration took;
- the summary gives total wall clock and total estimated cost.

The cost is Claude Code's estimate at **API list price**, not what you paid.
On a subscription, label it that way on any slide ("≈ $X at API prices"), or
show tokens and turns instead.

The screen is quiet while the agent works: `claude -p` returns only when the
iteration is over. The full JSON for each iteration stays in
`.ralph/logs/iter-NN.log` until the next reset; `run.csv` survives resets and
keeps one row per iteration of every run.

Pin the model for anything you plan to show, so the slide and the log agree.
Iterations take minutes each (the oracle alone boots Spring and runs vitest),
so a run from 0/68 is measured in hours.

## Demo branch: see what the loop built

This `demo` branch starts at `run-2026-09-23`, the last commit of the recorded
run, and adds one human commit on top. The app code the agent wrote
(`backend/src/main/java`, `frontend/src`) is not changed.

    powershell -ExecutionPolicy Bypass -File .\demo.ps1

That starts the backend on in-memory H2 with a seeded demo account, then the
page on http://localhost:5173, and opens the browser. Ctrl+C stops both. The
backend logs to `backend\target\demo-backend.log`. Use `-BackendPort` if 8081
is taken (the default avoids 8080, which other software often holds), and
`-NoBrowser` to skip opening the browser.

`-ExecutionPolicy Bypass` applies to that one process only. Windows blocks
`.ps1` scripts by default; the same form works for `ralph.ps1`.

What the human commit changes, and why:

- **The production build.** `npm run build` failed on the start-state
  `vite.config.ts` (TS2769: vitest 2 bundles its own vite, and the `test`
  block did not type-check against vite 6). The oracle never ran the build,
  so the loop finished green without noticing. The test setup now lives in
  `vitest.config.ts`, outside tsconfig, and the build passes. This was a
  harness bug, not the agent's; branch `harness-v2` fixes it for future runs.
- **A way to run it without MySQL.** `application-demo.yml` (profile `demo`)
  uses H2 and seeds account 1 from `db/demo/`, because the schema seeds towns
  but no accounts. The dev proxy target comes from `API_URL`.

Still true after this commit: `python verify.py` is 68/68 GREEN, the 49
backend and 14 frontend unit tests pass, and the oracle files are untouched.

## Running it for real

    # database (optional - the tests use H2)
    docker compose -f db/docker-compose.yml up -d

    # backend on :8080
    cd backend && mvn spring-boot:run

    # frontend on :5173, proxying /api to the backend
    cd frontend && npm install && npm run dev

## Verified, and not

- **Verified:** the whole 68-case oracle passes against a reference
  implementation, which is how the specs were checked for self-consistency
  before Ralph ever saw them. That implementation was then removed — the loop
  has to earn it back.
- **Not verified:** the MySQL path. `db/docker-compose.yml` and the MySQL
  profile have never been run, because Docker was not available during setup.
  The Flyway migration is exercised on H2 only. It is the first item under
  "Discovered work" in `fix_plan.md`.

## Reference

Geoffrey Huntley's original write-up: https://ghuntley.com/ralph/
