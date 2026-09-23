package ots.charcreate.unit.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ots.charcreate.character.NameInvalidCharactersException;
import ots.charcreate.character.NameReservedWordException;
import ots.charcreate.character.NameTooLongException;
import ots.charcreate.character.NameTooManyWordsException;
import ots.charcreate.character.NameTooShortException;
import ots.charcreate.character.NameValidator;
import ots.charcreate.character.NameWordTooShortException;

class NameValidatorTest {

    @Test
    void trimsLeadingAndTrailingWhitespace() {
        assertEquals("Padded Name", NameValidator.normalizeAndValidate("  Padded Name  "));
    }

    @Test
    void collapsesInternalWhitespaceRuns() {
        assertEquals("Wide Gap", NameValidator.normalizeAndValidate("Wide    Gap"));
    }

    @Test
    void preservesCase() {
        assertEquals("eLiTe kNiGhT", NameValidator.normalizeAndValidate("eLiTe kNiGhT"));
    }

    @Test
    void rejectsNameShorterThanThreeCharacters() {
        assertThrows(NameTooShortException.class, () -> NameValidator.normalizeAndValidate("Ab"));
    }

    @Test
    void rejectsNameShorterThanThreeCharactersAfterTrim() {
        assertThrows(NameTooShortException.class, () -> NameValidator.normalizeAndValidate("  Ab  "));
    }

    @Test
    void rejectsNameLongerThan29Characters() {
        assertThrows(NameTooLongException.class,
                () -> NameValidator.normalizeAndValidate("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
    }

    @Test
    void acceptsNameAtMaxLength() {
        String name = "Abbbbbbbbbbbbbbbbbbbbbbbbbbbb";
        assertEquals(name, NameValidator.normalizeAndValidate(name));
    }

    @Test
    void rejectsDigits() {
        assertThrows(NameInvalidCharactersException.class, () -> NameValidator.normalizeAndValidate("Player 1"));
    }

    @Test
    void rejectsPunctuation() {
        assertThrows(NameInvalidCharactersException.class, () -> NameValidator.normalizeAndValidate("O'Brien"));
    }

    @Test
    void rejectsNonAsciiLetters() {
        assertThrows(NameInvalidCharactersException.class, () -> NameValidator.normalizeAndValidate("Josué"));
    }

    @Test
    void rejectsMoreThanFourWords() {
        assertThrows(NameTooManyWordsException.class,
                () -> NameValidator.normalizeAndValidate("One Two Three Four Five"));
    }

    @Test
    void acceptsExactlyFourWords() {
        String name = "One Two Three Four";
        assertEquals(name, NameValidator.normalizeAndValidate(name));
    }

    @Test
    void rejectsWordShorterThanTwoCharacters() {
        assertThrows(NameWordTooShortException.class, () -> NameValidator.normalizeAndValidate("Bob X Smith"));
    }

    @Test
    void rejectsReservedWord() {
        assertThrows(NameReservedWordException.class, () -> NameValidator.normalizeAndValidate("God Slayer"));
    }

    @Test
    void rejectsReservedWordAsSubstring() {
        assertThrows(NameReservedWordException.class, () -> NameValidator.normalizeAndValidate("Godfrey Bold"));
    }

    @Test
    void rejectsReservedWordAfterSpaceRemoval() {
        assertThrows(NameReservedWordException.class, () -> NameValidator.normalizeAndValidate("Go dly"));
    }

    @Test
    void wordTooShortBeatsReservedWord() {
        assertThrows(NameWordTooShortException.class, () -> NameValidator.normalizeAndValidate("G o d"));
    }

    @Test
    void doesNotRejectTwoLetterAbbreviationsLikeGm() {
        assertEquals("Sigmund", NameValidator.normalizeAndValidate("Sigmund"));
    }

    @Test
    void tooShortBeatsInvalidCharacters() {
        assertThrows(NameTooShortException.class, () -> NameValidator.normalizeAndValidate("A1"));
    }
}
