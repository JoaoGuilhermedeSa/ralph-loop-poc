package ots.charcreate.character;

/** Sex id is not 0 or 1. */
public class InvalidSexException extends RuntimeException {

    public InvalidSexException(int sex) {
        super("Invalid sex: " + sex);
    }
}
