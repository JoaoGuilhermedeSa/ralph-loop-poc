package ots.charcreate.character;

/** Normalised name contains a reserved word as a substring, spaces removed. */
public class NameReservedWordException extends NameValidationException {

    public NameReservedWordException() {
        super("NAME_RESERVED_WORD", "Name contains a reserved word.");
    }
}
