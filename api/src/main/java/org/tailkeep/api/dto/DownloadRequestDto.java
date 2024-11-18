package org.tailkeep.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DownloadRequestDto(
    @NotNull(message = "URL cannot be null")
    @NotBlank(message = "URL cannot be empty")
    String url
) {} 
