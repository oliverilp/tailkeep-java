package org.tailkeep.api.dto;

import org.tailkeep.api.model.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    private String nickname;
    private String username;
    private String password;
    private Role role;
}
