package ots.charcreate.character;

/** Normalised name is longer than 29 characters. */
public class NameTooLongException extends NameValidationException {

    public NameTooLongException() {
        super("NAME_TOO_LONG", "Name must be at most 29 characters long.");
    }
}
