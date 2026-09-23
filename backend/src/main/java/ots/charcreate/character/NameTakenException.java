package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/**
 * The unique constraint on {@code players.name_key} rejected the insert - a
 * case-insensitive name collision, including a concurrent insert that won the
 * race. See {@code specs/02-name-rules.md}.
 */
public class NameTakenException extends ApiFieldException {

    public NameTakenException(String name) {
        super("NAME_TAKEN", "name", HttpStatus.CONFLICT, "Name already taken: " + name);
    }
}
