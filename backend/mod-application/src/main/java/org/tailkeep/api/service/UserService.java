package org.tailkeep.api.service;

import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tailkeep.api.model.user.Role;
import org.tailkeep.api.model.user.User;
import org.tailkeep.api.dto.ChangePasswordRequestDto;
import org.tailkeep.api.exception.InvalidCurrentPasswordException;
import org.tailkeep.api.exception.PasswordMismatchException;
import org.tailkeep.api.repository.UserRepository;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final ValidationService validationService;
    private final Environment env;

    public void changePassword(ChangePasswordRequestDto request, Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        
        // Check demo mode restrictions
        boolean isDemo = "true".equalsIgnoreCase(env.getProperty("DEMO_MODE"));
        if (isDemo && user.getRole() != Role.ADMIN) {
            throw new UnsupportedOperationException("Password change is disabled in demo mode");
        }

        validationService.validatePasswordLength(request.newPassword());
        validationService.validatePasswordLength(request.confirmationPassword());

        // check if the current password is correct
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException("Current password is incorrect");
        }
        // check if the two new passwords are the same
        if (!request.newPassword().equals(request.confirmationPassword())) {
            throw new PasswordMismatchException("New passwords do not match");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        // save the new password
        repository.save(user);
    }
}
