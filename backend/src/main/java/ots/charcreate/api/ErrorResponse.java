package ots.charcreate.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Uniform error body shape for the API, per {@code specs/03-api.md}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, String message, String field) {
}
