package ots.charcreate.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The oracle. Reads {@code acceptance/cases.json} and turns every case into its
 * own dynamic test, so the pass count is a real score and not one all-or-nothing
 * assertion.
 *
 * <p>Ralph may not edit this class or the case table. See {@code AGENTS.md}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AcceptanceTest {

    /** Accounts 1..3 exist for every case; 999 deliberately never does. */
    private static final int SEEDED_ACCOUNTS = 3;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final RestTemplate REST = new RestTemplate();

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @TestFactory
    Stream<DynamicTest> acceptanceCases() {
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode testCase : readCases()) {
            tests.add(DynamicTest.dynamicTest(testCase.get("id").asString(), () -> runCase(testCase)));
        }
        return tests.stream();
    }

    private JsonNode readCases() {
        try (InputStream in = getClass().getResourceAsStream("/acceptance/cases.json")) {
            if (in == null) {
                throw new IllegalStateException("acceptance/cases.json is missing from the test classpath");
            }
            return JSON.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("could not read acceptance/cases.json", e);
        }
    }

    private void runCase(JsonNode testCase) {
        resetDatabase();

        if (testCase.has("given")) {
            for (JsonNode setup : testCase.get("given")) {
                ResponseEntity<String> response = postCharacter(setup);
                assertEquals(201, response.getStatusCode().value(),
                        () -> "setup request should have succeeded: " + setup + "\n  got "
                                + response.getStatusCode().value() + " " + response.getBody());
            }
        }

        JsonNode expect = testCase.get("expect");
        ResponseEntity<String> response = testCase.has("get")
                ? exchange(HttpMethod.GET, testCase.get("get").asString(), null)
                : postCharacter(testCase.get("request"));

        assertEquals(expect.get("status").asInt(), response.getStatusCode().value(),
                () -> "wrong status\n  response body: " + response.getBody());

        if (expect.has("json")) {
            assertEquals(expect.get("json"), JSON.readTree(response.getBody()),
                    "whole response body should match");
        }

        if (expect.has("body")) {
            JsonNode actual = JSON.readTree(response.getBody());
            for (var field : expect.get("body").properties()) {
                JsonNode actualValue = actual.get(field.getKey());
                assertEquals(field.getValue(), actualValue,
                        () -> "field '" + field.getKey() + "'\n  full response: " + response.getBody());
            }
        }
    }

    private ResponseEntity<String> postCharacter(JsonNode body) {
        return exchange(HttpMethod.POST, "/api/characters", body.toString());
    }

    /**
     * A 4xx/5xx is an expected outcome here, not an exception - most cases assert
     * one. Catching {@link HttpStatusCodeException} keeps status and body in the
     * same shape as a success, without depending on a version-specific error
     * handler API.
     */
    private ResponseEntity<String> exchange(HttpMethod method, String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            return REST.exchange("http://localhost:" + port + path, method, entity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /** Every case starts from the same known-empty world. */
    private void resetDatabase() {
        jdbc.update("DELETE FROM players");
        jdbc.update("DELETE FROM accounts");
        for (int id = 1; id <= SEEDED_ACCOUNTS; id++) {
            jdbc.update("INSERT INTO accounts (id, name, password_hash, email) VALUES (?, ?, ?, ?)",
                    id, "account" + id, "not-a-real-hash", "account" + id + "@example.invalid");
        }
    }
}
