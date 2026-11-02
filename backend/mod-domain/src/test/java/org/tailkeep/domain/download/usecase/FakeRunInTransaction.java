package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.shared.RunInTransaction;

import java.util.function.Supplier;

/** Runs the work at once and counts how often it was asked to. */
public class FakeRunInTransaction implements RunInTransaction {
    public int runs;

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
