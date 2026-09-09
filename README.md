# Character creation for an Open Tibia server — built by a Ralph loop

A real feature, built the way the talk describes: a spec, an oracle the agent
cannot edit, a backlog, and a `while` loop.

The talk itself, and a 30-second toy version of the same harness, live in the
companion repo **`ralph-loop`**. This one is where the technique meets work
that actually has to ship.

    python verify.py        # 0/68 right now - nothing is implemented yet
    ./ralph.sh -n 12        # or: .\ralph.ps1 -Iterations 12

Then read what it did:

    git log --oneline ralph-start..HEAD
    cat .ralph/journal.md

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
| `tools/oracle_lock.py` | tripwire over the 16 files Ralph may not touch | locked |
| `backend/`, `frontend/` | where the agent actually writes | agent |

`db/migration/V1__init.sql` is locked too. `spring.jpa.hibernate.ddl-auto` is
`validate` in every profile, so an entity that disagrees with the schema fails
the context on startup rather than silently reshaping the database.

## Guardrails

- **Bounded budget** — `-n` / `-Iterations`, never `while true` unattended.
- **Stall detector** — three iterations with no commit and the loop halts.
- **Oracle tripwire** — the 16 protected files are hashed before and after
  every iteration; any change halts the run.
- **Score guard** — the total may not fall; a regression becomes the next
  iteration's only job.
- **Dirty-tree guard** — `--reset` refuses to discard uncommitted work.

Smoke detectors, not a sandbox. The real backstop is one small commit per
iteration that a human can read.

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
