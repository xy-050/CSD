package app.exception;

public class InvalidPasswordException extends RuntimeException {
    private static String defaultMsg = "Password must be at least 8 characters long, containing a mix of uppercase letters, lowercase letters, and special symbols.";

    public InvalidPasswordException() {
        super(defaultMsg);
    }

    public InvalidPasswordException(String msg) {
        super(msg + "\n" + defaultMsg);
    }
}
