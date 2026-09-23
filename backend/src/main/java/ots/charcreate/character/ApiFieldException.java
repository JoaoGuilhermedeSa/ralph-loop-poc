package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/**
 * Base for the API errors in {@code specs/03-api.md}'s error table that are not
 * name-rule violations: {@code FIELD_REQUIRED}, {@code INVALID_VOCATION},
 * {@code INVALID_SEX}, {@code UNKNOWN_ACCOUNT} and {@code UNKNOWN_TOWN}.
 */
public abstract class ApiFieldException extends RuntimeException {

    private final String errorCode;
    private final String field;
    private final HttpStatus status;

    protected ApiFieldException(String errorCode, String field, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.field = field;
        this.status = status;
    }

    public String errorCode() {
        return errorCode;
    }

    public String field() {
        return field;
    }

    public HttpStatus status() {
        return status;
    }
}
