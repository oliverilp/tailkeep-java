package org.tailkeep.adapter.ytdlp.process;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class DefaultProcessFactory implements ProcessFactory {
    @Override
    public Process createProcess(List<String> command, File workingDirectory) throws IOException {
        return new ProcessBuilder(command)
                .directory(workingDirectory)
                .start();
    }
}
