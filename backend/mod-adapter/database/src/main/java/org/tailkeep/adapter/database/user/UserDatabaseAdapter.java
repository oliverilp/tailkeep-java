package org.tailkeep.adapter.database.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.domain.user.FindUser;
import org.tailkeep.domain.user.SaveUser;
import org.tailkeep.domain.user.User;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class UserDatabaseAdapter implements FindUser, SaveUser {
    private final UserJpaRepository repository;

    public UserDatabaseAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> byId(UUID id) {
        return repository.findById(id).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> byUsername(String username) {
        return repository.findByUsername(username).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Override
    public User save(User user) {
        return UserEntityMapper.toDomain(repository.saveAndFlush(UserEntityMapper.toEntity(user)));
    }
}
