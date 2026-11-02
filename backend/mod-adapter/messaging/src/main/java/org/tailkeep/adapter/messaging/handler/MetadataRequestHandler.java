package org.tailkeep.adapter.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.MetadataMessageMapper;
import org.tailkeep.adapter.messaging.contract.MetadataRequestMessage;
import org.tailkeep.domain.download.usecase.FetchMetadata;

/**
 * Handlers finish their work before they return and let exceptions out, so the broker adapter acknowledges a
 * message only after it succeeded, and redelivers it otherwise.
 */
@Component
@Slf4j
public class MetadataRequestHandler {
    private final FetchMetadata fetchMetadata;

    public MetadataRequestHandler(FetchMetadata fetchMetadata) {
        this.fetchMetadata = fetchMetadata;
    }

    public void handle(MetadataRequestMessage message) {
        log.info("Received message request with url: {}", message.url());

        fetchMetadata.execute(MetadataMessageMapper.toDomain(message));
    }
}
