package ots.charcreate.character;

import org.springframework.http.HttpStatus;

/** No account exists with the requested id. */
public class UnknownAccountException extends ApiFieldException {

    public UnknownAccountException(long accountId) {
        super("UNKNOWN_ACCOUNT", "accountId", HttpStatus.NOT_FOUND, "Unknown account: " + accountId);
    }
}
