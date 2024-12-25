package org.tailkeep.worker.download;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tailkeep.worker.command.CommandExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@Slf4j
public class DownloadService {
    private final CommandExecutor commandExecutor;

    public DownloadService(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public CompletableFuture<DownloadProgressMessage> processDownload(
            String jobId,
            String videoId,
            String url,
            String filename,
            Consumer<DownloadProgressMessage> onProgress) {
        var downloader = new Downloader(commandExecutor, videoId, jobId, url, filename);

        return downloader.download(onProgress);
    }
}
