package org.tailkeep.domain.download;

import org.tailkeep.domain.shared.RunInTransaction;

import java.util.function.Supplier;

/** Runs the work at once and counts how often it was asked to. */
class FakeRunInTransaction implements RunInTransaction {
    int runs;

    @Override
    public <T> T execute(Supplier<T> work) {
        runs++;
        return work.get();
    }

    @Override
    public void execute(Runnable work) {
        runs++;
        work.run();
    }
}
