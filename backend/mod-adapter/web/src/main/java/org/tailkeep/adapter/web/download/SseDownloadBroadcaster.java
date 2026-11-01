package org.tailkeep.adapter.web.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.tailkeep.domain.download.DownloadDetails;
import org.tailkeep.domain.download.SendDownloadToClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sends downloads to the clients connected to this instance. The open connections are the only state kept
 * in memory, which is allowed because they belong to this process. Other instances reach their own clients
 * through the messaging adapter.
 */
@Component
public class SseDownloadBroadcaster implements SendDownloadToClients {
    private static final Logger log = LoggerFactory.getLogger(SseDownloadBroadcaster.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> {
            log.info("SSE connection completed");
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.info("SSE connection timeout");
            emitter.complete();
            emitters.remove(emitter);
        });

        emitters.add(emitter);
        return emitter;
    }

    @Override
    public boolean hasClients() {
        return !emitters.isEmpty();
    }

    @Override
    public void send(DownloadDetails download) {
        DownloadResponse response = DownloadResponseMapper.toResponse(download);
        List<SseEmitter> dead = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(response));
            } catch (IOException e) {
                log.error("Error sending SSE event", e);
                dead.add(emitter);
            }
        });

        emitters.removeAll(dead);
    }
}
