package org.tailkeep.worker.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tailkeep.worker.command.ProcessFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Stands in for the yt-dlp process. {@code -j <url>} prints one line of metadata JSON, a plain {@code <url>}
 * prints download progress and leaves the video file in the media directory, as yt-dlp does.
 */
class FakeYtDlp implements ProcessFactory {
    static final String METADATA_RESOURCE = "/metadata/youtube-metadata-response.json";
    static final int FILE_SIZE = 3072;

    /** What yt-dlp prints while downloading, with its progress bar lines. */
    static final String DOWNLOAD_OUTPUT = String.join("\n",
            "[youtube] dQw4w9WgXcQ: Downloading webpage",
            "[info] dQw4w9WgXcQ: Downloading 1 format(s): 137+140",
            "[download] Destination: video.mp4",
            "[download]   0.0% of ~  50.00MiB at  Unknown B/s ETA Unknown (frag 0/10)",
            "[download]  12.3% of ~  50.00MiB at    2.00MiB/s ETA 00:25 (frag 1/10)",
            "[download]  67.8% of ~  50.00MiB at    3.50MiB/s ETA 00:05 (frag 7/10)",
            "[download] 100% of   50.00MiB in 00:15 at 3.30MiB/s",
            "");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String mediaPath;

    FakeYtDlp(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    static String filename() throws IOException {
        try (InputStream in = FakeYtDlp.class.getResourceAsStream(METADATA_RESOURCE)) {
            return new ObjectMapper().readTree(in).get("filename").asText();
        }
    }

    @Override
    public Process createProcess(List<String> command, File workingDirectory) throws IOException {
        if (command.contains("-j")) {
            try (InputStream in = getClass().getResourceAsStream(METADATA_RESOURCE)) {
                // yt-dlp prints the metadata as a single line
                return new FakeProcess(objectMapper.writeValueAsString(objectMapper.readTree(in)) + "\n");
            }
        }
        Files.write(new File(mediaPath, filename()).toPath(), new byte[FILE_SIZE]);
        return new FakeProcess(DOWNLOAD_OUTPUT);
    }

    private static class FakeProcess extends Process {
        private final String stdout;

        FakeProcess(String stdout) {
            this.stdout = stdout;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(stdout.getBytes());
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }

        @Override
        public int waitFor() {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }
    }
}
