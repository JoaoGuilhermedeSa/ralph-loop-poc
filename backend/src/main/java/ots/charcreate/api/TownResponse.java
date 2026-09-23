package ots.charcreate.api;

/** Response shape for {@code GET /api/towns}, per {@code specs/03-api.md}. */
public record TownResponse(int id, String name) {
}
