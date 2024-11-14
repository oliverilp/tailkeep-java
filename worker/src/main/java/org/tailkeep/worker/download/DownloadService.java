package org.tailkeep.worker.download;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DownloadService {
    private final DownloaderFactory downloaderFactory;

    public DownloadService(DownloaderFactory downloaderFactory) {
        this.downloaderFactory = downloaderFactory;
    }

    public CompletableFuture<DownloadProgress> processDownload(
            String jobId,
            String videoId,
            String url,
            String filename,
            Consumer<DownloadProgress> onProgress) {

        Downloader downloader = downloaderFactory.createDownloader(videoId, Long.parseLong(jobId), url, filename);
        return downloader.download(onProgress);
    }
}
