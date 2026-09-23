package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/** The account already has the maximum number of characters allowed. */
public class CharacterLimitReachedException extends ApiFieldException {

    public CharacterLimitReachedException(long accountId) {
        super("CHARACTER_LIMIT_REACHED", null, HttpStatus.CONFLICT,
                "Account " + accountId + " already has the maximum number of characters.");
    }
}
