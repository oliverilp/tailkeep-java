package org.tailkeep.domain.user.model;

public record PasswordChange(String currentPassword, String newPassword, String confirmationPassword) {
}
