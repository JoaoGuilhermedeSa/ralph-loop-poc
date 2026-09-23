package ots.charcreate.character;

/** Base for the six ordered name-rule violations in {@code specs/02-name-rules.md}. */
public abstract class NameValidationException extends RuntimeException {

    private final String errorCode;

    protected NameValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
