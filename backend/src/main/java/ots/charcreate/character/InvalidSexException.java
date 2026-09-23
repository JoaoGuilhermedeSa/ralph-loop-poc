package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/** Sex id is not 0 or 1. */
public class InvalidSexException extends ApiFieldException {

    public InvalidSexException(int sex) {
        super("INVALID_SEX", "sex", HttpStatus.BAD_REQUEST, "Invalid sex: " + sex);
    }
}
