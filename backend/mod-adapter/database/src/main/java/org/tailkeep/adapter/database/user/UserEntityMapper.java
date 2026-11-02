package org.tailkeep.adapter.database.user;

import org.tailkeep.domain.user.model.User;

public final class UserEntityMapper {
    private UserEntityMapper() {
    }

    public static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .nickname(entity.getNickname())
                .username(entity.getUsername())
                .passwordHash(entity.getPassword())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.id())
                .nickname(user.nickname())
                .username(user.username())
                .password(user.passwordHash())
                .role(user.role())
                .createdAt(user.createdAt())
                .updatedAt(user.updatedAt())
                .build();
    }
}
