package org.tailkeep.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertions on raw JSON. Tests use these instead of reading responses into DTOs, so renaming,
 * adding or removing a field fails them.
 */
public final class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {
    }

    public static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Not valid JSON: " + json, e);
        }
    }

    public static List<String> keys(JsonNode node) {
        List<String> keys = new ArrayList<>();
        node.fieldNames().forEachRemaining(keys::add);
        return keys;
    }

    /** The top-level {@code id} of each element of an array. */
    public static List<String> ids(JsonNode array) {
        List<String> ids = new ArrayList<>();
        array.forEach(element -> ids.add(element.get("id").asText()));
        return ids;
    }

    /** The object has exactly these keys, in any order. */
    public static JsonNode assertKeys(JsonNode node, String... expected) {
        assertThat(node.isObject()).as("expected a JSON object but was %s", node).isTrue();
        assertThat(keys(node)).containsExactlyInAnyOrderElementsOf(Arrays.asList(expected));
        return node;
    }

    public static void assertUuid(JsonNode node) {
        assertThat(node.isTextual()).as("uuid %s", node).isTrue();
        UUID.fromString(node.asText());
    }

    public static void assertTimestamp(JsonNode node) {
        assertThat(node.isTextual()).as("timestamp %s", node).isTrue();
        LocalDateTime.parse(node.asText());
    }

    public static void assertNullOrTimestamp(JsonNode node) {
        if (!node.isNull()) {
            assertTimestamp(node);
        }
    }

    /** The shape every error response has. */
    public static JsonNode assertError(String body, int status, String path, String message) {
        JsonNode error = assertKeys(parse(body), "path", "message", "statusCode", "timestamp");
        assertThat(error.get("statusCode").isInt()).isTrue();
        assertThat(error.get("statusCode").asInt()).isEqualTo(status);
        assertThat(error.get("path").asText()).isEqualTo(path);
        assertThat(error.get("message").asText()).isEqualTo(message);
        assertThat(error.get("timestamp").isTextual()).isTrue();
        return error;
    }
}
