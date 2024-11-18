package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.tailkeep.api.exception.PasswordLengthException;

@Service
public class ValidationService {
    private static final int MIN_PASSWORD_LENGTH = 10;
    private static final int MAX_PASSWORD_LENGTH = 100;

    public void validatePasswordLength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new PasswordLengthException(
                String.format("Password must be between %d and %d characters long", 
                    MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH)
            );
        }
    }
}
