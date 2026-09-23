package ots.charcreate.unit.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ots.charcreate.domain.Experience;
import org.junit.jupiter.api.Test;

class ExperienceTest {

    @Test
    void level1RequiresNoExperience() {
        assertEquals(0L, Experience.forLevel(1));
    }

    @Test
    void level2Requires100() {
        assertEquals(100L, Experience.forLevel(2));
    }

    @Test
    void level8Requires4200() {
        assertEquals(4200L, Experience.forLevel(8));
    }
}
