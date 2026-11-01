package org.tailkeep.api.dto;

import java.util.List;

public record PageResponseDto<T>(
    List<T> items,
    int totalPages,
    long totalItems,
    int currentPage,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious
) {} 
