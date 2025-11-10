package app.exception;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(String msg) {
        super(msg);
    }
}
