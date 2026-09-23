package ots.charcreate.domain;

/** The standard Tibia experience curve, per {@code specs/01-domain.md}. */
public final class Experience {

    private Experience() {
    }

    /** Total experience required to reach level {@code level}. */
    public static long forLevel(int level) {
        long l = level;
        return (((l - 6) * l + 17) * l - 12) / 6 * 100;
    }
}
