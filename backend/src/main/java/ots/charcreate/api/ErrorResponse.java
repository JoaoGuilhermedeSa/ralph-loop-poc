package ots.charcreate.api;

/** Uniform error body shape for the API, per {@code specs/03-api.md}. */
public record ErrorResponse(String error, String message, String field) {
}
