package org.tailkeep.adapter.web.download;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DownloadRequest(
        @NotNull(message = "URL cannot be null")
        @NotBlank(message = "URL cannot be empty")
        String url) {
}
