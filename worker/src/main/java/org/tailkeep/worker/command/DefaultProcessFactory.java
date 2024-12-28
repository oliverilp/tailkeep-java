package org.tailkeep.worker.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultProcessFactory implements ProcessFactory {
    @Override
    public Process createProcess(List<String> command, File workingDirectory) throws IOException {
        return new ProcessBuilder(command)
                .directory(workingDirectory)
                .start();
    }
} 
