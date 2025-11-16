package app.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordCheckerTest {

    @Test
    void isValidPassword_ReturnsTrueForStrongPassword() {
        assertTrue(PasswordChecker.isValidPassword("StrongPass1!"));
    }

    @Test
    void isValidPassword_FalseWhenMissingUppercase() {
        assertFalse(PasswordChecker.isValidPassword("weakpass1!"));
    }

    @Test
    void isValidPassword_FalseWhenMissingDigit() {
        assertFalse(PasswordChecker.isValidPassword("Weakpass!"));
    }

    @Test
    void isValidPassword_FalseWhenMissingSpecial() {
        assertFalse(PasswordChecker.isValidPassword("Weakpass1"));
    }

    @Test
    void isValidPassword_FalseWhenTooShort() {
        assertFalse(PasswordChecker.isValidPassword("Aa1!"));
    }

    @Test
    void isValidPassword_FalseWhenNull() {
        assertFalse(PasswordChecker.isValidPassword(null));
    }
}



