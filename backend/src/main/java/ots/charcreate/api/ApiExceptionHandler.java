package ots.charcreate.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ots.charcreate.character.NameValidationException;

/**
 * Translates domain exceptions into the error body shape {@code specs/03-api.md}
 * requires, instead of Spring's default error body. Only name-rule violations are
 * wired up so far - the rest of the error table (FIELD_REQUIRED, INVALID_VOCATION,
 * INVALID_SEX, UNKNOWN_ACCOUNT, UNKNOWN_TOWN, NAME_TAKEN, CHARACTER_LIMIT_REACHED)
 * is a separate backlog item.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NameValidationException.class)
    public ResponseEntity<ErrorResponse> handleNameValidation(NameValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.errorCode(), ex.getMessage(), "name"));
    }
}
