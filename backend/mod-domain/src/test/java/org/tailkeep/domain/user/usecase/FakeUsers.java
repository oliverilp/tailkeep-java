package org.tailkeep.domain.user.usecase;

import org.tailkeep.domain.user.model.Role;
import org.tailkeep.domain.user.model.User;
import org.tailkeep.domain.user.port.FindUser;
import org.tailkeep.domain.user.port.SaveUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FakeUsers implements FindUser, SaveUser {
    public final List<User> saved = new ArrayList<>();

    public User add(String username, String passwordHash, Role role) {
        return save(User.builder().nickname(username).username(username).passwordHash(passwordHash).role(role).build());
    }

    @Override
    public Optional<User> byId(UUID id) {
        return saved.stream().filter(user -> user.id().equals(id)).findFirst();
    }

    @Override
    public Optional<User> byUsername(String username) {
        return saved.stream().filter(user -> user.username().equals(username)).findFirst();
    }

    @Override
    public long count() {
        return saved.size();
    }

    @Override
    public User save(User user) {
        User stored = user.id() != null ? user : user.toBuilder().id(UUID.randomUUID()).build();
        saved.removeIf(existing -> existing.id().equals(stored.id()));
        saved.add(stored);
        return stored;
    }
}
