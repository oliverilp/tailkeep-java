package org.tailkeep.domain.shared;

import java.util.List;

/** One page of results. Pages start at 1, as in the REST API. */
public record PageResult<T>(List<T> items, int page, int size, long totalItems) {
    public PageResult {
        items = List.copyOf(items);
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalItems / size);
    }

    public boolean hasNext() {
        return page < totalPages();
    }

    public boolean hasPrevious() {
        return page > 1;
    }
}
