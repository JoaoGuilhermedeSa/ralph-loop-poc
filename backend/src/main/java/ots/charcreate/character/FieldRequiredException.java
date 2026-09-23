package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/** A required request field is missing or null. */
public class FieldRequiredException extends ApiFieldException {

    public FieldRequiredException(String field) {
        super("FIELD_REQUIRED", field, HttpStatus.BAD_REQUEST, field + " is required.");
    }
}
