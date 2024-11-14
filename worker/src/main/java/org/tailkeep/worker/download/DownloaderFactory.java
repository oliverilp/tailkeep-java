package org.tailkeep.worker.download;

import org.springframework.stereotype.Service;
import org.tailkeep.worker.command.CommandExecutor;

@Service
public class DownloaderFactory {
    private final CommandExecutor commandExecutor;

    public DownloaderFactory(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public Downloader createDownloader(String videoId, long jobId, String url, String filename) {
        return new Downloader(commandExecutor, videoId, jobId, url, filename);
    }
}
