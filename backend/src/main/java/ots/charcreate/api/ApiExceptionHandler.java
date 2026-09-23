package ots.charcreate.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ots.charcreate.character.ApiFieldException;
import ots.charcreate.character.NameValidationException;

/**
 * Translates domain exceptions into the error body shape {@code specs/03-api.md}
 * requires, instead of Spring's default error body. {@code NAME_TAKEN} and
 * {@code CHARACTER_LIMIT_REACHED} are not wired up yet - see {@code fix_plan.md}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NameValidationException.class)
    public ResponseEntity<ErrorResponse> handleNameValidation(NameValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.errorCode(), ex.getMessage(), "name"));
    }

    @ExceptionHandler(ApiFieldException.class)
    public ResponseEntity<ErrorResponse> handleApiField(ApiFieldException ex) {
        return ResponseEntity.status(ex.status())
                .body(new ErrorResponse(ex.errorCode(), ex.getMessage(), ex.field()));
    }
}
