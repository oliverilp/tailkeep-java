package org.tailkeep.domain.shared;

import java.util.Objects;

/** Orders results by a field of the domain model. */
public record Sort(String property, Direction direction) {
    public enum Direction {
        ASC, DESC
    }

    public Sort {
        Objects.requireNonNull(property, "property");
        Objects.requireNonNull(direction, "direction");
    }

    public static Sort ascending(String property) {
        return new Sort(property, Direction.ASC);
    }

    public static Sort descending(String property) {
        return new Sort(property, Direction.DESC);
    }
}
