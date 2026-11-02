package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.Job;

public interface SaveJob {
    /** Inserts the job when it has no id, otherwise updates it, with its progress. Returns the saved job. */
    Job save(Job job);
}
