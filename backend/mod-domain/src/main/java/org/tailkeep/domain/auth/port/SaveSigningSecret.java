package org.tailkeep.domain.auth.port;

public interface SaveSigningSecret {
    void save(String secret);
}
