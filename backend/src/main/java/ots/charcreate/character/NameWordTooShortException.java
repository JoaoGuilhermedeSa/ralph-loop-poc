package ots.charcreate.character;

/** A word in the normalised name is shorter than 2 characters. */
public class NameWordTooShortException extends NameValidationException {

    public NameWordTooShortException() {
        super("NAME_WORD_TOO_SHORT", "Every word in the name must be at least 2 characters long.");
    }
}
