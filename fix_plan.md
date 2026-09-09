# fix_plan — the backlog

The loop's working memory. Ralph reads this at the top of every iteration, does
exactly **one** unchecked item, ticks it, and stops.

Priority is top-to-bottom. Reorder this file to steer the run — that is the
intended way to change Ralph's mind, and it is cheaper than arguing with it.

Scores in brackets are the rough acceptance gain to expect, out of 68. They are
a sanity check for the human reading `git log`, not a target to game.

## Backlog

- [ ] **Domain model** — `Vocation` (per-level health/mana/capacity gains and
      outfit), `Outfit`, `Sex`, and the experience-for-level formula, all per
      `specs/01-domain.md`. Start levels come from configuration, not
      constants. Unit-test the formula against level 1, 2 and 8. *[+0]*

- [ ] **Persistence** — JPA entities and repositories for `players`, `towns`
      and `accounts` matching `V1__init.sql` exactly. `ddl-auto: validate`
      means the context fails to start if a column is wrong; getting it to
      start is the deliverable. Map column names explicitly. *[+0]*

- [ ] **Reference endpoints** — `GET /api/vocations` and `GET /api/towns` per
      `specs/03-api.md`, both ordered by id. *[+2]*

- [ ] **Create a character, happy path** — `POST /api/characters` returning
      201 with every stat resolved: level, experience, health, mana, capacity,
      lookType, outfit colours and the town's spawn position. This is the bulk
      of the score. *[+18]*

- [ ] **Name normalisation and rules** — trim, collapse internal whitespace,
      preserve case; then rules 1–6 of `specs/02-name-rules.md` **in the order
      given**, each with its own error code. *[+16]*

- [ ] **The rest of the error table** — required fields, invalid vocation and
      sex, unknown account and town, and the cross-group validation order in
      `specs/03-api.md`. One error shape, no Spring default bodies, no 500s
      for input the API is meant to reject. *[+10]*

- [ ] **Uniqueness and the character limit** — case-insensitive `name_key`
      collisions as 409 `NAME_TAKEN` (catch the constraint violation; a lost
      race must not surface as a 500), and 10 characters per account as 409
      `CHARACTER_LIMIT_REACHED`. *[+6]*

- [ ] **Frontend scaffold** — `App.tsx` with the four controls and the
      `data-testid` values from `specs/04-frontend.md`, loading vocations and
      towns on mount, defaulting to Sorcerer / male / Thais, every control
      labelled. *[+4]*

- [ ] **Frontend submit and result** — client-side mirror of the name rules,
      POST with the configured `accountId`, and the result panel showing the
      created character's stats with a "Create another" button. *[+5]*

- [ ] **Frontend error handling** — render the server's `message`, mark the
      name field `aria-invalid` when the error names it, keep what the player
      typed, and show a retry message when the network fails. *[+3]*

## Discovered work (not scheduled)

Append here instead of widening the current iteration.

- Docker path is unverified: `db/docker-compose.yml` and the MySQL profile
  have never been run (Docker was not available when this was set up). The H2
  test path is verified. Someone should boot it once against real MySQL.

## Blocked

Nothing.
