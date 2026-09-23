package ots.charcreate.character;

/** Normalised name contains something other than ASCII letters and single spaces. */
public class NameInvalidCharactersException extends NameValidationException {

    public NameInvalidCharactersException() {
        super("NAME_INVALID_CHARACTERS", "Name may only contain letters and single spaces.");
    }
}
