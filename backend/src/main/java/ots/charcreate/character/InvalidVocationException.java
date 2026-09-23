package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/** Vocation id is not one of the five defined in {@code specs/01-domain.md}. */
public class InvalidVocationException extends ApiFieldException {

    public InvalidVocationException(int vocation) {
        super("INVALID_VOCATION", "vocation", HttpStatus.BAD_REQUEST, "Invalid vocation: " + vocation);
    }
}
