package ots.charcreate.character;

/** No account exists with the requested id. */
public class UnknownAccountException extends RuntimeException {

    public UnknownAccountException(long accountId) {
        super("Unknown account: " + accountId);
    }
}
