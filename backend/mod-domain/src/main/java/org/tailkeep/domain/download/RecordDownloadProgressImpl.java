package org.tailkeep.domain.download;

import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.FindVideo;

import java.time.Clock;
import java.time.LocalDateTime;

@UseCase
public class RecordDownloadProgressImpl implements RecordDownloadProgress {
    private final FindJob findJob;
    private final SaveJob saveJob;
    private final BroadcastDownloadProgress broadcastDownloadProgress;
    private final DownloadDetailsAssembler assembler;
    private final Clock clock;

    public RecordDownloadProgressImpl(
            FindJob findJob,
            SaveJob saveJob,
            FindVideo findVideo,
            FindChannel findChannel,
            BroadcastDownloadProgress broadcastDownloadProgress,
            Clock clock) {
        this.findJob = findJob;
        this.saveJob = saveJob;
        this.broadcastDownloadProgress = broadcastDownloadProgress;
        this.assembler = new DownloadDetailsAssembler(findVideo, findChannel);
        this.clock = clock;
    }

    @Override
    public DownloadDetails execute(ProgressUpdate update) {
        Job job = update.hasEnded() ? complete(update) : reportProgress(update);

        DownloadDetails saved = assembler.assemble(saveJob.save(job).progress());
        broadcastDownloadProgress.broadcast(saved);
        return saved;
    }

    private Job reportProgress(ProgressUpdate update) {
        return findJob.byId(update.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", update.jobId().toString()))
                .reportProgress(update);
    }

    private Job complete(ProgressUpdate update) {
        return findJob.byId(update.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Download progress", update.jobId().toString()))
                .complete(update.size(), LocalDateTime.now(clock));
    }
}
