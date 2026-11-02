package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.download.port.FindDownload;
import org.tailkeep.domain.download.port.SendDownloadToClients;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.port.FindVideo;

import java.util.UUID;

@UseCase
public class PushDownloadProgressImpl implements PushDownloadProgress {
    private final SendDownloadToClients sendDownloadToClients;
    private final FindDownload findDownload;
    private final DownloadDetailsAssembler assembler;

    public PushDownloadProgressImpl(
            SendDownloadToClients sendDownloadToClients,
            FindDownload findDownload,
            FindVideo findVideo,
            FindChannel findChannel) {
        this.sendDownloadToClients = sendDownloadToClients;
        this.findDownload = findDownload;
        this.assembler = new DownloadDetailsAssembler(findVideo, findChannel);
    }

    @Override
    public void execute(UUID jobId) {
        if (!sendDownloadToClients.hasClients()) {
            return;
        }

        findDownload.byJobId(jobId)
                .map(assembler::assemble)
                .ifPresent(sendDownloadToClients::send);
    }
}
