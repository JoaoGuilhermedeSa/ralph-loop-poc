package ots.charcreate.character;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalises and validates a character name per {@code specs/02-name-rules.md}.
 * Rule 7 (uniqueness, {@code NAME_TAKEN}) is a database concern handled elsewhere.
 */
public final class NameValidator {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 29;
    private static final int MAX_WORDS = 4;
    private static final int MIN_WORD_LENGTH = 2;
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[A-Za-z]+( [A-Za-z]+)*");
    private static final List<String> RESERVED_WORDS = List.of("god", "admin", "administrator", "gamemaster",
            "tutor", "counsellor", "counselor", "staff", "owner", "support", "system", "null", "undefined");

    private NameValidator() {
    }

    public static String normalizeAndValidate(String rawName) {
        String name = normalize(rawName);

        if (name.length() < MIN_LENGTH) {
            throw new NameTooShortException();
        }
        if (name.length() > MAX_LENGTH) {
            throw new NameTooLongException();
        }
        if (!ALLOWED_CHARACTERS.matcher(name).matches()) {
            throw new NameInvalidCharactersException();
        }

        String[] words = name.split(" ");
        if (words.length > MAX_WORDS) {
            throw new NameTooManyWordsException();
        }
        for (String word : words) {
            if (word.length() < MIN_WORD_LENGTH) {
                throw new NameWordTooShortException();
            }
        }

        String withoutSpaces = name.replace(" ", "").toLowerCase(Locale.ROOT);
        for (String reserved : RESERVED_WORDS) {
            if (withoutSpaces.contains(reserved)) {
                throw new NameReservedWordException();
            }
        }

        return name;
    }

    private static String normalize(String rawName) {
        return rawName.trim().replaceAll("\\s+", " ");
    }
}
