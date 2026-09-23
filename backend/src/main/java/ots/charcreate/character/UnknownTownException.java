package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/** No town exists with the requested id. */
public class UnknownTownException extends ApiFieldException {

    public UnknownTownException(int townId) {
        super("UNKNOWN_TOWN", "townId", HttpStatus.NOT_FOUND, "Unknown town: " + townId);
    }
}
