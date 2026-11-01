package org.tailkeep.api.dto;

public record PageRequestDto(
    int page,
    int size,
    String progress
) {} 
