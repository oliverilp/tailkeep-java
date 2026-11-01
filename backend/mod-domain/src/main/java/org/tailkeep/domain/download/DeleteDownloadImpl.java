package org.tailkeep.domain.download;

import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.shared.UseCase;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@UseCase
public class DeleteDownloadImpl implements DeleteDownload {
    private final FindJob findJob;
    private final SaveJob saveJob;
    private final Clock clock;

    public DeleteDownloadImpl(FindJob findJob, SaveJob saveJob, Clock clock) {
        this.findJob = findJob;
        this.saveJob = saveJob;
        this.clock = clock;
    }

    @Override
    public void execute(UUID jobId) {
        Job job = findJob.byId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Download progress", jobId.toString()));
        saveJob.save(job.softDelete(LocalDateTime.now(clock)));
    }
}
