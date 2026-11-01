package org.tailkeep.domain.download;

/** Outbound port that shows saved progress to everyone who is watching, such as the connected web clients. */
public interface BroadcastDownloadProgress {
    void broadcast(DownloadDetails download);
}
