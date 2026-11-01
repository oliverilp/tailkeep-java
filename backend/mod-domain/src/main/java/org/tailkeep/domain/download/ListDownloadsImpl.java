package org.tailkeep.domain.download;

import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.FindVideo;

import java.util.List;

@UseCase
public class ListDownloadsImpl implements ListDownloads {
    private final FindDownload findDownload;
    private final DownloadDetailsAssembler assembler;

    public ListDownloadsImpl(FindDownload findDownload, FindVideo findVideo, FindChannel findChannel) {
        this.findDownload = findDownload;
        this.assembler = new DownloadDetailsAssembler(findVideo, findChannel);
    }

    @Override
    public List<DownloadDetails> execute() {
        return assembler.assemble(findDownload.newestFirst());
    }
}
