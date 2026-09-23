package ots.charcreate.character;

/** No town exists with the requested id. */
public class UnknownTownException extends RuntimeException {

    public UnknownTownException(int townId) {
        super("Unknown town: " + townId);
    }
}
