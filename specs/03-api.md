# HTTP API

JSON in, JSON out, `Content-Type: application/json`. Base path `/api`.

## POST /api/characters

Request:

```json
{ "accountId": 1, "name": "Bubble", "vocation": 1, "sex": 1, "townId": 1 }
```

All five fields are required. A missing or null field is `FIELD_REQUIRED`.

### 201 Created

Body is the created character, with every stat resolved:

```json
{
  "id": 1,
  "accountId": 1,
  "name": "Bubble",
  "vocation": 1,
  "vocationName": "Sorcerer",
  "sex": 1,
  "townId": 1,
  "townName": "Thais",
  "level": 8,
  "experience": 4200,
  "health": 185,
  "healthMax": 185,
  "mana": 210,
  "manaMax": 210,
  "capacity": 470,
  "lookType": 130,
  "lookHead": 78,
  "lookBody": 68,
  "lookLegs": 58,
  "lookFeet": 76,
  "posX": 32369,
  "posY": 32241,
  "posZ": 7
}
```

`Location: /api/characters/{id}` is set.

### Errors

Every error body has the same shape:

```json
{ "error": "NAME_TOO_SHORT", "message": "Name must be at least 3 characters long.", "field": "name" }
```

`message` is human-readable and shown to the player as-is by the frontend.
`field` names the offending request field, or is omitted when none applies.

| status | code                       | when                                       |
|--------|----------------------------|--------------------------------------------|
| 400    | `FIELD_REQUIRED`           | a required field is missing or null         |
| 400    | `NAME_TOO_SHORT`           | see `02-name-rules.md`                      |
| 400    | `NAME_TOO_LONG`            |                                             |
| 400    | `NAME_INVALID_CHARACTERS`  |                                             |
| 400    | `NAME_TOO_MANY_WORDS`      |                                             |
| 400    | `NAME_WORD_TOO_SHORT`      |                                             |
| 400    | `NAME_RESERVED_WORD`       |                                             |
| 400    | `INVALID_VOCATION`         | vocation is not 0–4                         |
| 400    | `INVALID_SEX`              | sex is not 0 or 1                           |
| 404    | `UNKNOWN_ACCOUNT`          | no account with that id                     |
| 404    | `UNKNOWN_TOWN`             | no town with that id                        |
| 409    | `NAME_TAKEN`               | case-insensitive name collision             |
| 409    | `CHARACTER_LIMIT_REACHED`  | the account already has 10 characters       |

Validation order across field groups: required-fields first, then name rules
(in the order given in `02-name-rules.md`), then vocation, then sex, then
account, then town, then the character limit, then the uniqueness insert.

No stack traces, no Spring default error bodies, no 500s for input the API is
supposed to reject.

## GET /api/vocations

```json
[ { "id": 0, "name": "None" }, { "id": 1, "name": "Sorcerer" }, ... ]
```

Ordered by id ascending. All five.

## GET /api/towns

```json
[ { "id": 1, "name": "Thais" }, { "id": 2, "name": "Carlin" }, ... ]
```

Ordered by id ascending, read from the `towns` table.

## CORS

The dev frontend runs on `http://localhost:5173`. Allow it for `/api/**`.
