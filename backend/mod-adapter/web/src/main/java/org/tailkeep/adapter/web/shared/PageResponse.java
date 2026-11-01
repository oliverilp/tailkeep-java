package org.tailkeep.adapter.web.shared;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int totalPages,
        long totalItems,
        int currentPage,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious) {
}
