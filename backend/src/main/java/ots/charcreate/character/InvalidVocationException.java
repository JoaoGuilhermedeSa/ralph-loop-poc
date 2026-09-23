package ots.charcreate.character;

/** Vocation id is not one of the five defined in {@code specs/01-domain.md}. */
public class InvalidVocationException extends RuntimeException {

    public InvalidVocationException(int vocation) {
        super("Invalid vocation: " + vocation);
    }
}
