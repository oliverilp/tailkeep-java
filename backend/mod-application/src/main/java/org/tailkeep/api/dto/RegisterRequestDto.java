package org.tailkeep.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    @NotNull
    @NotBlank(message = "Nickname is required")
    private String nickname;

    @NotNull
    @NotBlank(message = "Username is required") 
    private String username;

    @NotNull
    @NotBlank(message = "Password is required")
    private String password;
}
