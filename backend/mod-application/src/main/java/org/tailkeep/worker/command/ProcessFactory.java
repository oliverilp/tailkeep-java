package org.tailkeep.worker.command;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ProcessFactory {
    Process createProcess(List<String> command, File workingDirectory) throws IOException;
} 
