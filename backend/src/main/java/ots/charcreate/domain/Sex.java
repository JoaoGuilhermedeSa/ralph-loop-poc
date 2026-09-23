package ots.charcreate.domain;

import java.util.Optional;

/** Character sex, per {@code specs/01-domain.md}. Drives outfit selection. */
public enum Sex {
    FEMALE(0),
    MALE(1);

    private final int id;

    Sex(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static Optional<Sex> byId(int id) {
        for (Sex sex : values()) {
            if (sex.id == id) {
                return Optional.of(sex);
            }
        }
        return Optional.empty();
    }
}
