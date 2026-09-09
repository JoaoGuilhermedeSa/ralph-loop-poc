# Character creation — overview

A web page where the owner of an account creates a new character on an Open
Tibia server: pick a name, a vocation, a sex and a starting town; get a
character row with the correct starting stats.

## Shape

    frontend/   React + Vite. One form, client-side validation, result panel.
    backend/    Spring Boot 4 (Java 21 target). REST API + JPA + Flyway.
    db/         MySQL 8 for dev via docker-compose. H2 in-memory for tests.

## Scope

Character creation only. An account already exists; we do not build
registration, login, sessions, character lists or deletion. `accountId`
arrives in the request body and is validated for existence, nothing more.

If you think auth belongs here, append it to `fix_plan.md`. Do not build it.

## Schema is greenfield

This does not target a specific TFS release. The schema in `05-schema.md` is
TFS-*shaped* — the column names and starting-stat formulas match TFS
conventions so that a later adapter to a real distro is mechanical — but it is
ours, and `db/migration/V1__init.sql` is the authority.

## Definition of done

`python verify.py` is green: every acceptance case in
`backend/src/test/resources/acceptance/cases.json` passes, the frontend
acceptance suite passes, and both projects build.
