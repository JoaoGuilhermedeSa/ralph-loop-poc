# Domain: vocations, starting stats, outfits

These numbers are the whole point of the feature. Every one of them is pinned
by an acceptance case; none of them may be guessed.

## Vocations

| id | name     | starts at level |
|----|----------|-----------------|
| 0  | None     | 1               |
| 1  | Sorcerer | 8               |
| 2  | Druid    | 8               |
| 3  | Paladin  | 8               |
| 4  | Knight   | 8               |

Vocation `None` is the "rookgaard" start. Vocations 1–4 start at level 8,
which is the common Open Tibia configuration.

## Starting stats

A level-1 character of any vocation has **health 150, mana 0, capacity 400**.
Each level gained adds a per-vocation amount:

| vocation | health / level | mana / level | capacity / level |
|----------|----------------|--------------|------------------|
| None     | 5              | 5            | 10               |
| Sorcerer | 5              | 30           | 10               |
| Druid    | 5              | 30           | 10               |
| Paladin  | 10             | 15           | 20               |
| Knight   | 15             | 5            | 25               |

A character created at level `L` has gained `L - 1` levels. So:

    health   = 150 + (L - 1) * healthPerLevel
    mana     =   0 + (L - 1) * manaPerLevel
    capacity = 400 + (L - 1) * capPerLevel

`health == healthMax` and `mana == manaMax` on a fresh character.

Resolved, for the levels above:

| vocation | level | health | mana | capacity | experience |
|----------|-------|--------|------|----------|------------|
| None     | 1     | 150    | 0    | 400      | 0          |
| Sorcerer | 8     | 185    | 210  | 470      | 4200       |
| Druid    | 8     | 185    | 210  | 470      | 4200       |
| Paladin  | 8     | 220    | 105  | 540      | 4200       |
| Knight   | 8     | 255    | 35   | 575      | 4200       |

Capacity is stored in whole oz, not in units of 100.

## Experience

Total experience required to *reach* level `L` is the standard Tibia curve:

    exp(L) = (((L - 6) * L + 17) * L - 12) / 6 * 100

Integer arithmetic throughout; the division is exact for every positive `L`.
`exp(1) = 0`, `exp(2) = 100`, `exp(8) = 4200`. Implement the formula — do not
hardcode 4200, because the start level is configuration.

## Sex and outfits

| id | sex    |
|----|--------|
| 0  | female |
| 1  | male   |

`lookType` is chosen from vocation and sex:

| vocation | outfit  | male | female |
|----------|---------|------|--------|
| None     | Citizen | 128  | 136    |
| Sorcerer | Mage    | 130  | 138    |
| Druid    | Mage    | 130  | 138    |
| Paladin  | Hunter  | 129  | 137    |
| Knight   | Knight  | 131  | 139    |

Outfit colours are fixed for every new character:
`lookHead 78`, `lookBody 68`, `lookLegs 58`, `lookFeet 76`.

## Starting position

The character spawns at its town's temple position: `posX`, `posY`, `posZ` are
copied from the `towns` row identified by `townId`.

## Configuration

Start levels live in configuration (`app.start-level.default: 8`,
`app.start-level.vocation-none: 1`), not in the code. The acceptance suite
runs against the defaults above.
