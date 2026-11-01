package org.tailkeep.domain.user;

public record PasswordChange(String currentPassword, String newPassword, String confirmationPassword) {
}
