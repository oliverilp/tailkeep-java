package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.download.model.DownloadDetails;
import org.tailkeep.domain.download.port.FindDownload;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.port.FindVideo;

import java.util.UUID;

@UseCase
public class GetDownloadImpl implements GetDownload {
    private final FindDownload findDownload;
    private final DownloadDetailsAssembler assembler;

    public GetDownloadImpl(FindDownload findDownload, FindVideo findVideo, FindChannel findChannel) {
        this.findDownload = findDownload;
        this.assembler = new DownloadDetailsAssembler(findVideo, findChannel);
    }

    @Override
    public DownloadDetails execute(UUID jobId) {
        return findDownload.byJobId(jobId)
                .map(assembler::assemble)
                .orElseThrow(() -> new ResourceNotFoundException("Download progress", jobId.toString()));
    }
}
