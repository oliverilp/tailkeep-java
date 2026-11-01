package org.tailkeep.domain.shared;

import java.util.Objects;

/** Asks for one page of results. Pages start at 1, as in the REST API. */
public record PageQuery(int page, int size, Sort sort) {
    public PageQuery {
        if (page < 1) {
            throw new IllegalArgumentException("page must be at least 1, was " + page);
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1, was " + size);
        }
        Objects.requireNonNull(sort, "sort");
    }
}
