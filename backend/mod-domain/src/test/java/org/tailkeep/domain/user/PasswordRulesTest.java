package org.tailkeep.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordRulesTest {

    @Test
    void acceptsTheLengthLimits() {
        assertThatCode(() -> PasswordRules.requireValidLength("a".repeat(10))).doesNotThrowAnyException();
        assertThatCode(() -> PasswordRules.requireValidLength("a".repeat(100))).doesNotThrowAnyException();
    }

    @Test
    void refusesWhatIsOutsideThem() {
        for (String password : new String[] {null, "a".repeat(9), "a".repeat(101)}) {
            assertThatThrownBy(() -> PasswordRules.requireValidLength(password))
                    .isInstanceOf(PasswordLengthException.class)
                    .hasMessage("Password must be between 10 and 100 characters long");
        }
    }
}
