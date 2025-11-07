package app.exception;

public class UserConflictException extends Exception {
    public UserConflictException(String msg) {
        super(msg);
    }
}
