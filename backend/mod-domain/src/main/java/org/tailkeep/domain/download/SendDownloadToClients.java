package org.tailkeep.domain.download;

/** Outbound port that sends a download to the clients connected to this instance, such as the SSE streams. */
public interface SendDownloadToClients {
    /** Whether any client is connected here, so nothing needs loading when nobody is listening. */
    boolean hasClients();

    void send(DownloadDetails download);
}
