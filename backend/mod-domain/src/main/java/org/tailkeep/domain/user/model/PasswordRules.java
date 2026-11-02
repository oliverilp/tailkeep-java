package org.tailkeep.domain.user.model;

public final class PasswordRules {
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 100;

    private PasswordRules() {
    }

    public static void requireValidLength(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new PasswordLengthException(
                    String.format("Password must be between %d and %d characters long", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
