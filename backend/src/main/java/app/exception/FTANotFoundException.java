package app.exception;

public class FTANotFoundException extends RuntimeException {
    public FTANotFoundException(String message) {
        super(message);
    }
}
