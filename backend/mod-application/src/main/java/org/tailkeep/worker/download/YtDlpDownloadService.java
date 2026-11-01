package org.tailkeep.worker.download;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tailkeep.worker.command.CommandExecutor;
import org.tailkeep.api.message.DownloadProgressMessage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@Slf4j
public class YtDlpDownloadService {
    private final CommandExecutor commandExecutor;

    public YtDlpDownloadService(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public CompletableFuture<DownloadProgressMessage> processDownload(
            UUID jobId,
            String videoId,
            String url,
            String filename,
            Consumer<DownloadProgressMessage> onProgress) {
        var downloader = new Downloader(commandExecutor, videoId, jobId, url, filename);

        return downloader.download(onProgress);
    }
}
