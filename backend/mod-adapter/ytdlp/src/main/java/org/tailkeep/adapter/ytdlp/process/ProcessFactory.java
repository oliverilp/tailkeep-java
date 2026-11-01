package org.tailkeep.adapter.ytdlp.process;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Starts the operating system process. Tests replace it to fake yt-dlp. */
public interface ProcessFactory {
    Process createProcess(List<String> command, File workingDirectory) throws IOException;
}
