package ots.charcreate.domain;

/**
 * A vocation's starting outfit. Colours are not part of this - every new
 * character gets the same fixed colours, per {@code specs/01-domain.md}.
 */
public record Outfit(String name, int maleLookType, int femaleLookType) {

    public int lookTypeFor(Sex sex) {
        return sex == Sex.MALE ? maleLookType : femaleLookType;
    }
}
