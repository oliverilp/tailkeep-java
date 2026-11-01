package org.tailkeep.adapter.web.download;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
public class SseController {
    private final SseDownloadBroadcaster broadcaster;

    public SseController(SseDownloadBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return broadcaster.subscribe();
    }
}
