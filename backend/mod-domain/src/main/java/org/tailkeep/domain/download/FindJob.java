package org.tailkeep.domain.download;

import java.util.Optional;
import java.util.UUID;

public interface FindJob {
    Optional<Job> byId(UUID id);
}
