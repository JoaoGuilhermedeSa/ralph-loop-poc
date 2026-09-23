# House rules

Hard constraints. They exist because the loop runs unattended and one bad
iteration can otherwise burn the whole run.

## Never

- **Never modify the oracle.** That means:
  - `verify.py`
  - `backend/src/test/java/ots/charcreate/acceptance/**`
  - `backend/src/test/resources/acceptance/**`
  - `frontend/tests/acceptance/**`
  - `tools/gen_cases.py`

  `tools/oracle_lock.py` hashes these and the loop halts if they change. An
  agent that can edit its own exam will eventually pass it dishonestly — not
  from malice; deleting a failing test is a locally valid way to go green.

- **Never modify `specs/**` or `backend/src/main/resources/db/migration/**`.**
  The specification and the schema are the human's steering wheel. If you
  believe a spec is wrong, write `BLOCKED:` in `fix_plan.md` and stop.

- **Never delete or skip a failing test** to make the build green.

- **Never `git push`, `git reset --hard`, or rewrite history.**

- **Never add a dependency** unless a backlog item explicitly asks for it.

- **Never set `ddl-auto` to anything but `validate`.** Hibernate does not own
  this schema; Flyway does.

## Always

- One backlog item per iteration. Discovered work is appended, not done.
- Commit before you stop, even for a partial-but-green step.
- Keep the API's error shape exactly as `specs/03-api.md` defines it. A Spring
  default error body leaking to the frontend is a bug.
- Prefer a boring, readable solution. Your successor has no context and has to
  understand your code cold.

## Where things go

| you may write | you may not |
|---|---|
| `backend/src/main/java/**` | `specs/**` |
| `backend/src/main/resources/application*.yml` | `**/db/migration/**` |
| `backend/src/test/java/ots/charcreate/unit/**` | `**/acceptance/**` |
| `frontend/src/**` | `verify.py`, `tools/**` |
| `frontend/tests/unit/**` | `AGENTS.md`, `PROMPT.md` |
| `fix_plan.md`, `.ralph/journal.md` | `ralph.ps1`, `ralph.sh`, `.ralph/oracle.lock` |

## Style

- Java 21, constructor injection, no field injection, no Lombok.
- React function components with hooks, TypeScript strict, no UI framework.
- Domain rules live in the domain layer, not in controllers or components.
