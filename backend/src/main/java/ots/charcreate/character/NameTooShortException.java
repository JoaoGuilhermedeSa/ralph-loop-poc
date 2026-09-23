package ots.charcreate.character;

/** Normalised name is shorter than 3 characters. */
public class NameTooShortException extends NameValidationException {

    public NameTooShortException() {
        super("NAME_TOO_SHORT", "Name must be at least 3 characters long.");
    }
}
