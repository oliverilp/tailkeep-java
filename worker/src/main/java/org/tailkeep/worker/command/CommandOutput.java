package org.tailkeep.worker.command;

@FunctionalInterface
public interface CommandOutput {
    void onData(String data);
}
