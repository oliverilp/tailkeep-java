package org.tailkeep.support;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** HTTP client that returns status, headers and the body as text. */
public class Api {
    public record Response(int status, HttpHeaders headers, String body) {
        public JsonNode json() {
            return Json.parse(body);
        }
    }

    private final TestRestTemplate rest;

    public Api(TestRestTemplate rest) {
        this.rest = rest;
    }

    public Response get(String path) {
        return get(path, null);
    }

    public Response get(String path, String token) {
        return send(HttpMethod.GET, path, null, token);
    }

    public Response post(String path, String json, String token) {
        return send(HttpMethod.POST, path, json, token);
    }

    public Response patch(String path, String json, String token) {
        return send(HttpMethod.PATCH, path, json, token);
    }

    public Response delete(String path, String token) {
        return send(HttpMethod.DELETE, path, null, token);
    }

    public Response send(HttpMethod method, String path, String json, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        if (json != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        ResponseEntity<String> response = rest.exchange(path, method, new HttpEntity<>(json, headers), String.class);
        return new Response(response.getStatusCode().value(), response.getHeaders(), response.getBody());
    }
}
