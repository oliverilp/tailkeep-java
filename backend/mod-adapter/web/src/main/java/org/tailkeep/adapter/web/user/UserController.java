package org.tailkeep.adapter.web.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.domain.user.ChangePassword;
import org.tailkeep.domain.user.CurrentUser;
import org.tailkeep.domain.user.PasswordChange;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final ChangePassword changePassword;
    private final CurrentUser currentUser;

    public UserController(ChangePassword changePassword, CurrentUser currentUser) {
        this.changePassword = changePassword;
        this.currentUser = currentUser;
    }

    @PatchMapping
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        changePassword.execute(currentUser.id(), new PasswordChange(
                request.currentPassword(), request.newPassword(), request.confirmationPassword()));
        return ResponseEntity.ok().build();
    }
}
