package org.tailkeep.domain.channel;

public interface SaveChannel {
    /** Inserts the channel when it has no id, otherwise updates it. Returns the saved channel. */
    Channel save(Channel channel);
}
