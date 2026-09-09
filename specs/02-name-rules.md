# Character name rules

Names are the part players notice and the part that is hardest to change
later, so the rules are strict and every one of them is an acceptance case.

## Normalisation (before validation)

1. Trim leading and trailing whitespace.
2. Collapse every run of internal whitespace to a single space.

The normalised form is what gets validated and stored. Case is preserved
exactly as the player typed it: `"eLiTe kNiGhT"` stores as `"eLiTe kNiGhT"`.

## Validation, in this order

The first failing rule wins — a name that is both too short and contains a
digit reports `NAME_TOO_SHORT`. The order below is the order to check.

| # | rule                                              | error code               |
|---|---------------------------------------------------|--------------------------|
| 1 | normalised length ≥ 3                             | `NAME_TOO_SHORT`         |
| 2 | normalised length ≤ 29                            | `NAME_TOO_LONG`          |
| 3 | only ASCII letters `A–Z a–z` and single spaces    | `NAME_INVALID_CHARACTERS`|
| 4 | at most 4 space-separated words                   | `NAME_TOO_MANY_WORDS`    |
| 5 | every word is at least 2 characters long          | `NAME_WORD_TOO_SHORT`    |
| 6 | contains no reserved word (see below)             | `NAME_RESERVED_WORD`     |
| 7 | not already taken, case-insensitively             | `NAME_TAKEN`             |

Rule 7 is a database concern and returns **409**, not 400. Rules 1–6 return
**400**. See `03-api.md`.

## Reserved words

Case-insensitive **substring** match against the normalised name with spaces
removed, so `"G o d"` and `"Godlike"` are both rejected:

    god, admin, administrator, gamemaster, tutor, counsellor,
    counselor, staff, owner, support, system, null, undefined

Substring matching over-rejects on purpose. A player who wanted `Godfrey` can
pick another name; a player who gets `Gamemaster Bob` cannot be un-scammed.

Deliberately **not** on the list: two-letter abbreviations like `gm` and `cm`.
Under substring matching they would reject ordinary names — `Sigmund` contains
`gm` — and the words they stand for are already covered in full.

## Uniqueness

Case-insensitive. The `players` table carries a `name_key` column holding the
lowercased normalised name, with a `UNIQUE` constraint. The application fills
it on insert (not a generated column - it has to behave identically on MySQL
and H2). The check is the constraint, not a prior `SELECT`. A concurrent insert that loses the race must
surface as `NAME_TAKEN` / 409, never as a 500.

Two characters may not differ only by case or by whitespace runs:
`"Test Char"`, `"test char"` and `"Test  Char"` are all the same name.
