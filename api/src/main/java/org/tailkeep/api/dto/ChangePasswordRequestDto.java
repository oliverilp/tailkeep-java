package org.tailkeep.api.dto;

public record ChangePasswordRequestDto(String currentPassword, String newPassword, String confirmationPassword) {

}
