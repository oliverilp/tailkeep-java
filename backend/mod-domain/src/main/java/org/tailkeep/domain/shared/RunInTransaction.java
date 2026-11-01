package org.tailkeep.domain.shared;

import java.util.function.Supplier;

/**
 * Outbound port that makes the ports used inside {@code work} succeed or fail together.
 * <p>
 * A use case that changes several things at once calls this, and does side effects such as publishing a
 * message after it returns, so a message never leaves before the data is committed. A single save or
 * fetch needs no transaction, because the adapter handles its own.
 */
public interface RunInTransaction {
    <T> T execute(Supplier<T> work);

    void execute(Runnable work);
}
