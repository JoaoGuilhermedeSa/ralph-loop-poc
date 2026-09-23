package ots.charcreate.domain;

import java.util.Optional;

/**
 * The five vocations and their per-level gains, per {@code specs/01-domain.md}.
 * Start level is configuration, not part of this enum - see
 * {@code app.start-level.*}.
 */
public enum Vocation {
    NONE(0, "None", 5, 5, 10, new Outfit("Citizen", 128, 136)),
    SORCERER(1, "Sorcerer", 5, 30, 10, new Outfit("Mage", 130, 138)),
    DRUID(2, "Druid", 5, 30, 10, new Outfit("Mage", 130, 138)),
    PALADIN(3, "Paladin", 10, 15, 20, new Outfit("Hunter", 129, 137)),
    KNIGHT(4, "Knight", 15, 5, 25, new Outfit("Knight", 131, 139));

    private static final int BASE_HEALTH = 150;
    private static final int BASE_MANA = 0;
    private static final int BASE_CAPACITY = 400;

    private final int id;
    private final String displayName;
    private final int healthPerLevel;
    private final int manaPerLevel;
    private final int capacityPerLevel;
    private final Outfit outfit;

    Vocation(int id, String displayName, int healthPerLevel, int manaPerLevel, int capacityPerLevel, Outfit outfit) {
        this.id = id;
        this.displayName = displayName;
        this.healthPerLevel = healthPerLevel;
        this.manaPerLevel = manaPerLevel;
        this.capacityPerLevel = capacityPerLevel;
        this.outfit = outfit;
    }

    public int id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Outfit outfit() {
        return outfit;
    }

    public int healthAtLevel(int level) {
        return BASE_HEALTH + (level - 1) * healthPerLevel;
    }

    public int manaAtLevel(int level) {
        return BASE_MANA + (level - 1) * manaPerLevel;
    }

    public int capacityAtLevel(int level) {
        return BASE_CAPACITY + (level - 1) * capacityPerLevel;
    }

    public int lookType(Sex sex) {
        return outfit.lookTypeFor(sex);
    }

    public static Optional<Vocation> byId(int id) {
        for (Vocation vocation : values()) {
            if (vocation.id == id) {
                return Optional.of(vocation);
            }
        }
        return Optional.empty();
    }
}
