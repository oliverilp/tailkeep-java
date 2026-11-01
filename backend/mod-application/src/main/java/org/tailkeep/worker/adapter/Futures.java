package org.tailkeep.worker.adapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

// Legacy: the domain ports are synchronous, the yt-dlp code is not yet
final class Futures {
    private Futures() {
    }

    /** Waits for the future and fails with the original exception rather than a {@link CompletionException}. */
    static <T> T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}
