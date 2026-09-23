package ots.charcreate.api;

/** Request body for {@code POST /api/characters}, per {@code specs/03-api.md}. */
public record CreateCharacterRequest(Long accountId, String name, Integer vocation, Integer sex, Integer townId) {
}
