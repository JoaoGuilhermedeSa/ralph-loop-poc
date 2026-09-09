#!/usr/bin/env python3
"""Generate the backend acceptance case table from the rules in specs/.

The expected numbers are *derived* here from the formulas in
specs/01-domain.md rather than transcribed, so a typo in the table is not
possible - only a wrong formula, which is a spec bug and shows up as an
unsatisfiable case the moment a reference implementation is written.

    python tools/gen_cases.py        # rewrites backend/.../acceptance/cases.json

Ralph may not run this and may not edit its output. See AGENTS.md.
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "backend" / "src" / "test" / "resources" / "acceptance" / "cases.json"

# --- specs/01-domain.md ------------------------------------------------------
VOCATIONS = {
    0: {"name": "None",     "level": 1, "hp": 5,  "mp": 5,  "cap": 10, "outfit": "citizen"},
    1: {"name": "Sorcerer", "level": 8, "hp": 5,  "mp": 30, "cap": 10, "outfit": "mage"},
    2: {"name": "Druid",    "level": 8, "hp": 5,  "mp": 30, "cap": 10, "outfit": "mage"},
    3: {"name": "Paladin",  "level": 8, "hp": 10, "mp": 15, "cap": 20, "outfit": "hunter"},
    4: {"name": "Knight",   "level": 8, "hp": 15, "mp": 5,  "cap": 25, "outfit": "knight"},
}
LOOKTYPE = {  # outfit -> (female, male)
    "citizen": (136, 128),
    "hunter":  (137, 129),
    "mage":    (138, 130),
    "knight":  (139, 131),
}
COLOURS = {"lookHead": 78, "lookBody": 68, "lookLegs": 58, "lookFeet": 76}
TOWNS = {
    1: ("Thais",        32369, 32241,  7),
    2: ("Carlin",       32360, 31782,  7),
    3: ("Venore",       32957, 32076,  7),
    4: ("Ab'Dendriel",  32732, 31634,  7),
    5: ("Kazordoon",    32649, 31925, 11),
    6: ("Rookgaard",    32097, 32219,  7),
}


def experience(level: int) -> int:
    return (((level - 6) * level + 17) * level - 12) // 6 * 100


def expected_character(name: str, vocation: int, sex: int, town_id: int) -> dict:
    voc = VOCATIONS[vocation]
    level = voc["level"]
    gained = level - 1
    town_name, px, py, pz = TOWNS[town_id]
    body = {
        "name": name,
        "vocation": vocation,
        "vocationName": voc["name"],
        "sex": sex,
        "townId": town_id,
        "townName": town_name,
        "level": level,
        "experience": experience(level),
        "health": 150 + gained * voc["hp"],
        "healthMax": 150 + gained * voc["hp"],
        "mana": 0 + gained * voc["mp"],
        "manaMax": 0 + gained * voc["mp"],
        "capacity": 400 + gained * voc["cap"],
        "lookType": LOOKTYPE[voc["outfit"]][sex],
        "posX": px, "posY": py, "posZ": pz,
    }
    body.update(COLOURS)
    return body


def post(case_id: str, request: dict, status: int, body: dict, given=None) -> dict:
    case = {"id": case_id, "request": request, "expect": {"status": status, "body": body}}
    if given:
        case["given"] = given
    return case


def req(name="Bubble", vocation=1, sex=1, town=1, account=1) -> dict:
    return {"accountId": account, "name": name, "vocation": vocation, "sex": sex, "townId": town}


def err(code: str, field: str | None = None) -> dict:
    return {"error": code} if field is None else {"error": code, "field": field}


def build() -> list[dict]:
    cases: list[dict] = []

    # -- every vocation, full stat block ------------------------------------
    for voc_id, voc in VOCATIONS.items():
        name = voc["name"] + " Test"
        cases.append(post(
            f"create-{voc['name'].lower()}-male",
            req(name=name, vocation=voc_id, sex=1),
            201, expected_character(name, voc_id, 1, 1),
        ))

    # -- outfit depends on sex ----------------------------------------------
    for voc_id, voc in VOCATIONS.items():
        name = voc["name"] + " Lady"
        cases.append(post(
            f"outfit-{voc['name'].lower()}-female",
            req(name=name, vocation=voc_id, sex=0),
            201, {"lookType": LOOKTYPE[voc["outfit"]][0], "sex": 0, **COLOURS},
        ))

    # -- spawn position comes from the town ---------------------------------
    ordinals = {1: "One", 2: "Two", 3: "Three", 4: "Four", 5: "Five", 6: "Six"}
    for town_id, (town_name, px, py, pz) in TOWNS.items():
        cases.append(post(
            f"spawn-town-{town_id}",
            req(name=f"Pilgrim {ordinals[town_id]}", town=town_id),
            201, {"townId": town_id, "townName": town_name, "posX": px, "posY": py, "posZ": pz},
        ))

    # -- name normalisation --------------------------------------------------
    cases += [
        post("name-trimmed",           req(name="  Padded Name  "), 201, {"name": "Padded Name"}),
        post("name-spaces-collapsed",  req(name="Wide    Gap"),     201, {"name": "Wide Gap"}),
        post("name-case-preserved",    req(name="eLiTe kNiGhT"),    201, {"name": "eLiTe kNiGhT"}),
    ]

    # -- name rules, in the order specs/02-name-rules.md gives them ----------
    cases += [
        post("name-too-short",          req(name="Ab"),                   400, err("NAME_TOO_SHORT", "name")),
        post("name-too-short-after-trim", req(name="  Ab  "),             400, err("NAME_TOO_SHORT", "name")),
        post("name-too-long",           req(name="A" * 30),               400, err("NAME_TOO_LONG", "name")),
        post("name-max-length-ok",      req(name="A" + "b" * 28),         201, {"name": "A" + "b" * 28}),
        post("name-digits",             req(name="Player 1"),             400, err("NAME_INVALID_CHARACTERS", "name")),
        post("name-punctuation",        req(name="O'Brien"),              400, err("NAME_INVALID_CHARACTERS", "name")),
        post("name-plain-ascii-ok",     req(name="Josue Ferrao"),         201, {"name": "Josue Ferrao"}),
        post("name-non-ascii",          req(name="Josué"),                400, err("NAME_INVALID_CHARACTERS", "name")),
        post("name-five-words",         req(name="One Two Three Four Five"), 400, err("NAME_TOO_MANY_WORDS", "name")),
        post("name-four-words-ok",      req(name="One Two Three Four"),   201, {"name": "One Two Three Four"}),
        post("name-single-letter-word", req(name="Bob X Smith"),          400, err("NAME_WORD_TOO_SHORT", "name")),
        post("name-reserved-god",       req(name="God Slayer"),           400, err("NAME_RESERVED_WORD", "name")),
        post("name-reserved-substring", req(name="Godfrey Bold"),         400, err("NAME_RESERVED_WORD", "name")),
        post("name-reserved-spaced",    req(name="Go dly"),               400, err("NAME_RESERVED_WORD", "name")),
        post("name-order-word-beats-reserved", req(name="G o d"),         400, err("NAME_WORD_TOO_SHORT", "name")),
        post("name-reserved-admin",     req(name="Admin Helper"),         400, err("NAME_RESERVED_WORD", "name")),
        post("name-gm-substring-allowed", req(name="Sigmund"),            201, {"name": "Sigmund"}),
        # "Towner" contains "owner". Substring matching over-rejects by design;
        # this case exists so that behaviour is pinned rather than discovered.
        post("name-reserved-owner-substring", req(name="Towner One"),     400, err("NAME_RESERVED_WORD", "name")),
        # rule order: too-short wins over invalid-characters
        post("name-order-short-beats-chars", req(name="A1"),              400, err("NAME_TOO_SHORT", "name")),
    ]

    # -- required fields -----------------------------------------------------
    for field in ("accountId", "name", "vocation", "sex", "townId"):
        body = req()
        del body[field]
        cases.append(post(f"required-{field}", body, 400, err("FIELD_REQUIRED", field)))

    # -- enum validation -----------------------------------------------------
    cases += [
        post("vocation-too-high", req(vocation=5),  400, err("INVALID_VOCATION", "vocation")),
        post("vocation-negative", req(vocation=-1), 400, err("INVALID_VOCATION", "vocation")),
        post("sex-invalid",       req(sex=2),       400, err("INVALID_SEX", "sex")),
    ]

    # -- referential ---------------------------------------------------------
    cases += [
        post("unknown-account", req(account=999), 404, err("UNKNOWN_ACCOUNT", "accountId")),
        post("unknown-town",    req(town=99),     404, err("UNKNOWN_TOWN", "townId")),
    ]

    # -- uniqueness ----------------------------------------------------------
    cases += [
        post("name-taken-exact", req(name="Duplicate Hero"), 409, err("NAME_TAKEN", "name"),
             given=[req(name="Duplicate Hero")]),
        post("name-taken-different-case", req(name="dUPLICATE hERO"), 409, err("NAME_TAKEN", "name"),
             given=[req(name="Duplicate Hero")]),
        post("name-taken-different-spacing", req(name="Duplicate    Hero"), 409, err("NAME_TAKEN", "name"),
             given=[req(name="Duplicate Hero")]),
        post("different-name-ok", req(name="Duplicate Heroes"), 201, {"name": "Duplicate Heroes"},
             given=[req(name="Duplicate Hero")]),
    ]

    # -- per-account character limit ----------------------------------------
    cases.append(post(
        "character-limit", req(name="Eleventh Char"), 409, err("CHARACTER_LIMIT_REACHED"),
        given=[req(name=f"Filler Char{chr(ord('a') + i)}") for i in range(10)],
    ))
    cases.append(post(
        "character-limit-boundary", req(name="Tenth Char"), 201, {"name": "Tenth Char"},
        given=[req(name=f"Filler Char{chr(ord('a') + i)}") for i in range(9)],
    ))

    # -- reference data ------------------------------------------------------
    cases.append({
        "id": "list-vocations",
        "get": "/api/vocations",
        "expect": {"status": 200, "json": [{"id": i, "name": v["name"]} for i, v in VOCATIONS.items()]},
    })
    cases.append({
        "id": "list-towns",
        "get": "/api/towns",
        "expect": {"status": 200, "json": [{"id": i, "name": n} for i, (n, *_rest) in TOWNS.items()]},
    })

    return cases


def main() -> None:
    cases = build()
    ids = [c["id"] for c in cases]
    duplicates = {i for i in ids if ids.count(i) > 1}
    if duplicates:
        raise SystemExit(f"duplicate case ids: {sorted(duplicates)}")
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(cases, indent=2) + "\n", encoding="utf-8")
    print(f"{len(cases)} acceptance cases -> {OUT.relative_to(ROOT).as_posix()}")


if __name__ == "__main__":
    main()
