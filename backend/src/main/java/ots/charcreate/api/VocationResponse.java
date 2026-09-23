package ots.charcreate.api;

/** Response shape for {@code GET /api/vocations}, per {@code specs/03-api.md}. */
public record VocationResponse(int id, String name) {
}
