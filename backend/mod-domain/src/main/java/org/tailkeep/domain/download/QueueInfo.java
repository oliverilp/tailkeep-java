package org.tailkeep.domain.download;

/** How many downloads are waiting, running and finished. */
public record QueueInfo(long queue, long active, long finished) {
}
