package app.security;

public class PasswordChecker {
    /**
     * Checks if the password meets the minimum requirements.
     * The requirements are:
     * - At least 8 characters long
     * - Contains at least one uppercase letter
     * - Contains at least one lowercase letter
     * - Contains at least one digit
     * - Contains at least one special character (!@#$%^&*()-+)
     * 
     * @param password The password to check.
     * @return true if the password is valid, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false; // Minimum length requirement
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isDigit(ch)) hasDigit = true;
            else if ("!@#$%^&*()-+".indexOf(ch) >= 0) hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
