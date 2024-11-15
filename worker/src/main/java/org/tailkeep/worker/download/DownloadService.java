package org.tailkeep.worker.download;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.tailkeep.worker.command.CommandExecutor;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DownloadService {
    private final CommandExecutor commandExecutor;

    public DownloadService(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public CompletableFuture<DownloadProgress> processDownload(
            String jobId,
            String videoId,
            String url,
            String filename,
            Consumer<DownloadProgress> onProgress) {
        var downloader = new Downloader(commandExecutor, videoId, jobId, url, filename);

        return downloader.download(onProgress);
    }
}
