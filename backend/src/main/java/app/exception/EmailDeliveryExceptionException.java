package app.exception;
/**
 * Thrown when email fails to send due to mail server issues
 */
public class EmailDeliveryExceptionException extends EmailServiceException {
    public EmailDeliveryExceptionException(String message, Throwable cause) {
        super(message, cause);
    }
}