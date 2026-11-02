package org.tailkeep.adapter.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.MetadataMessageMapper;
import org.tailkeep.adapter.messaging.contract.MetadataResultMessage;
import org.tailkeep.domain.download.usecase.RecordMetadata;

@Component
@Slf4j
public class MetadataResultHandler {
    private final RecordMetadata recordMetadata;

    public MetadataResultHandler(RecordMetadata recordMetadata) {
        this.recordMetadata = recordMetadata;
    }

    public void handle(MetadataResultMessage message) {
        log.info("Received metadata result: {}", message);

        recordMetadata.execute(MetadataMessageMapper.toDomain(message));

        log.info("Sent download request for job: {}", message.jobId());
    }
}
