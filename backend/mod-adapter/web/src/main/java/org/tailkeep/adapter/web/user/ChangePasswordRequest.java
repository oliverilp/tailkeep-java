package org.tailkeep.adapter.web.user;

public record ChangePasswordRequest(String currentPassword, String newPassword, String confirmationPassword) {
}
