package org.tailkeep.worker.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.tailkeep.worker.command.CommandExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MetadataFetcher {
    private final CommandExecutor cmd;
    private final ObjectMapper objectMapper;

    public MetadataFetcher(CommandExecutor cmd, ObjectMapper objectMapper) {
        this.cmd = cmd;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<Metadata> fetch(String url) {
        StringBuilder json = new StringBuilder();
        List<String> args = Arrays.asList("-j", url);
        return cmd.execute(args, json::append)
                .thenApply(unused -> parseMetadata(json.toString()));
    }

    private Metadata parseMetadata(String jsonStr) {
        try {
            JsonNode json = objectMapper.readTree(jsonStr);
            log.info("Metadata finished " + json.get("title").asText());

            return buildMetadata(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse metadata", e);
        }
    }

    private Metadata buildMetadata(JsonNode json) {
        return new Metadata(
                json.get("id").asText(),
                json.get("webpage_url").asText(),
                json.get("title").asText(),
                json.get("uploader").asText(),
                json.get("channel_id").asText(),
                json.get("channel_url").asText(),
                json.get("duration_string").asText(),
                json.get("duration").asDouble(),
                json.get("thumbnail").asText(),
                json.path("description").asText(""),
                json.path("view_count").asLong(0),
                json.path("comment_count").asLong(0),
                json.get("filename").asText());
    }
}
