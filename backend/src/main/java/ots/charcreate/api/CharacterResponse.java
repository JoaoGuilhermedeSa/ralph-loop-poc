package ots.charcreate.api;

/** Response body for a created character, per {@code specs/03-api.md}. */
public record CharacterResponse(
        Long id,
        Long accountId,
        String name,
        int vocation,
        String vocationName,
        int sex,
        int townId,
        String townName,
        int level,
        long experience,
        int health,
        int healthMax,
        int mana,
        int manaMax,
        int capacity,
        int lookType,
        int lookHead,
        int lookBody,
        int lookLegs,
        int lookFeet,
        int posX,
        int posY,
        int posZ) {
}
