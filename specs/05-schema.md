# Database

**`backend/src/main/resources/db/migration/V1__init.sql` is the authority.**
This file explains it; where they disagree, the migration wins.

## Engines

| environment | engine                                    |
|-------------|-------------------------------------------|
| tests       | H2 in-memory, `MODE=MySQL`                |
| dev / prod  | MySQL 8 (`db/docker-compose.yml`)         |

One migration must apply cleanly to both. That constraint is why `name_key` is
an ordinary column the application fills rather than a generated column, and
why there are no MySQL-only types in the schema.

Flyway owns the schema. `spring.jpa.hibernate.ddl-auto` is `validate` in every
profile — Hibernate never creates or alters a table. If an entity and the
migration disagree, the app must fail to start; that is the point.

## Tables

**`accounts`** — pre-existing in the real world; we only read it. Character
creation validates that `account_id` exists and counts existing characters.

**`towns`** — reference data, seeded by the migration with six Tibia towns and
their temple positions. A new character's `pos_x/pos_y/pos_z` are copied from
its town.

**`players`** — the character. Column names follow TFS conventions
(`look_type`, `health_max`, `pos_x`) so that an adapter to a real distro later
is a rename exercise rather than a redesign.

Notable constraints:

- `uq_players_name_key` — the real enforcement of case-insensitive name
  uniqueness. Catch the constraint violation and map it to `NAME_TAKEN` / 409.
- `fk_players_account`, `fk_players_town` — referential integrity. The API
  still checks both up front so it can return a useful 404 instead of a 500.

## Test data

Acceptance tests seed their own accounts. The migration seeds **towns only** —
never accounts or players, because a fixture that exists in production is a
bug waiting to happen.
