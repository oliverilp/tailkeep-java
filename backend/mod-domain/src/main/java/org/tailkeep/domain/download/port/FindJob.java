package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.Job;

import java.util.Optional;
import java.util.UUID;

public interface FindJob {
    Optional<Job> byId(UUID id);
}
