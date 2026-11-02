package org.tailkeep.adapter.web.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull
        @NotBlank(message = "Nickname is required")
        String nickname,
        @NotNull
        @NotBlank(message = "Username is required")
        String username,
        @NotNull
        @NotBlank(message = "Password is required")
        String password) {
}
