package ots.charcreate.character;

/** Normalised name has more than 4 space-separated words. */
public class NameTooManyWordsException extends NameValidationException {

    public NameTooManyWordsException() {
        super("NAME_TOO_MANY_WORDS", "Name may have at most 4 words.");
    }
}
