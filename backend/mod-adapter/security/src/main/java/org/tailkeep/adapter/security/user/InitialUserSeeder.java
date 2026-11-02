package org.tailkeep.adapter.security.user;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tailkeep.domain.user.usecase.SeedInitialUser;

/** Runs while the context starts, so the first user exists before any request can arrive. */
@Component
public class InitialUserSeeder {
    private static final Logger log = LoggerFactory.getLogger(InitialUserSeeder.class);

    private final SeedInitialUser seedInitialUser;

    public InitialUserSeeder(SeedInitialUser seedInitialUser) {
        this.seedInitialUser = seedInitialUser;
    }

    @PostConstruct
    public void seed() {
        seedInitialUser.execute().ifPresent(user -> log.info("Seeded {} user", user.username()));
    }
}
