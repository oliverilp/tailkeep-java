package org.tailkeep.domain.download.model;

/** How many downloads are waiting, running and finished. */
public record QueueInfo(long queue, long active, long finished) {
}
