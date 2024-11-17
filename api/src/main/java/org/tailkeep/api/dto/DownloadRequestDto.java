package org.tailkeep.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DownloadRequestDto(
    @NotBlank(message = "URL cannot be empty")
    String url
) {} 
