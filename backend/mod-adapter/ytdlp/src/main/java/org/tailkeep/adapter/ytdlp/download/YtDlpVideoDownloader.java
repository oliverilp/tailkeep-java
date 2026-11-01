package org.tailkeep.adapter.ytdlp.download;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.ytdlp.config.YtDlpProperties;
import org.tailkeep.adapter.ytdlp.process.YtDlpRunner;
import org.tailkeep.domain.download.DownloadRequest;
import org.tailkeep.domain.download.ProgressUpdate;
import org.tailkeep.domain.download.VideoDownloader;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

/** Downloads with yt-dlp, which writes the video into the media directory. */
@Component
@Slf4j
public class YtDlpVideoDownloader implements VideoDownloader {
    private final YtDlpRunner runner;
    private final File mediaPath;

    public YtDlpVideoDownloader(YtDlpRunner runner, YtDlpProperties properties) {
        this.runner = runner;
        this.mediaPath = new File(properties.getPath());
    }

    @Override
    public ProgressUpdate download(DownloadRequest request, Consumer<ProgressUpdate> onProgress) {
        StdoutProgressParser parser = new StdoutProgressParser(request.youtubeId(), request.jobId());

        runner.run(List.of(request.url()), line -> {
            log.info(line);
            onProgress.accept(parser.onLine(line));
        });

        return parser.finish(sizeOnDisk(request.filename()));
    }

    private String sizeOnDisk(String filename) {
        try {
            return FileSize.formatSize(Files.size(new File(mediaPath, filename).toPath()));
        } catch (Exception e) {
            log.error("Error getting file size: {}", e.getMessage());
            return null;
        }
    }
}
