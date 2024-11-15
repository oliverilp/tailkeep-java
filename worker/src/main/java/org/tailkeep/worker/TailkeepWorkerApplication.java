package org.tailkeep.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.tailkeep.worker.download.Downloader;

@SpringBootApplication
public class TailkeepWorkerApplication {

    public static void main(String[] args) {
        // var downloader = new Downloader(null, "1", "1", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "test.mp4");
        // downloader.onOutput("[download]  10.0% of   39.97MiB at    7.43MiB/s ETA 00:04");
        // System.out.println(downloader.getDownloadProgress());

        SpringApplication.run(TailkeepWorkerApplication.class, args);
    }
}
