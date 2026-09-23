package ots.charcreate.unit.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ots.charcreate.domain.Sex;
import ots.charcreate.domain.Vocation;
import org.junit.jupiter.api.Test;

/** Pins the resolved stat table from {@code specs/01-domain.md}. */
class VocationTest {

    @Test
    void noneAtLevel1() {
        assertEquals(150, Vocation.NONE.healthAtLevel(1));
        assertEquals(0, Vocation.NONE.manaAtLevel(1));
        assertEquals(400, Vocation.NONE.capacityAtLevel(1));
    }

    @Test
    void sorcererAtLevel8() {
        assertEquals(185, Vocation.SORCERER.healthAtLevel(8));
        assertEquals(210, Vocation.SORCERER.manaAtLevel(8));
        assertEquals(470, Vocation.SORCERER.capacityAtLevel(8));
    }

    @Test
    void druidAtLevel8() {
        assertEquals(185, Vocation.DRUID.healthAtLevel(8));
        assertEquals(210, Vocation.DRUID.manaAtLevel(8));
        assertEquals(470, Vocation.DRUID.capacityAtLevel(8));
    }

    @Test
    void paladinAtLevel8() {
        assertEquals(220, Vocation.PALADIN.healthAtLevel(8));
        assertEquals(105, Vocation.PALADIN.manaAtLevel(8));
        assertEquals(540, Vocation.PALADIN.capacityAtLevel(8));
    }

    @Test
    void knightAtLevel8() {
        assertEquals(255, Vocation.KNIGHT.healthAtLevel(8));
        assertEquals(35, Vocation.KNIGHT.manaAtLevel(8));
        assertEquals(575, Vocation.KNIGHT.capacityAtLevel(8));
    }

    @Test
    void lookTypesMatchOutfitTable() {
        assertEquals(128, Vocation.NONE.lookType(Sex.MALE));
        assertEquals(136, Vocation.NONE.lookType(Sex.FEMALE));
        assertEquals(130, Vocation.SORCERER.lookType(Sex.MALE));
        assertEquals(138, Vocation.SORCERER.lookType(Sex.FEMALE));
        assertEquals(130, Vocation.DRUID.lookType(Sex.MALE));
        assertEquals(138, Vocation.DRUID.lookType(Sex.FEMALE));
        assertEquals(129, Vocation.PALADIN.lookType(Sex.MALE));
        assertEquals(137, Vocation.PALADIN.lookType(Sex.FEMALE));
        assertEquals(131, Vocation.KNIGHT.lookType(Sex.MALE));
        assertEquals(139, Vocation.KNIGHT.lookType(Sex.FEMALE));
    }

    @Test
    void byIdResolvesKnownVocations() {
        assertEquals(Vocation.SORCERER, Vocation.byId(1).orElseThrow());
        assertTrue(Vocation.byId(99).isEmpty());
    }
}
