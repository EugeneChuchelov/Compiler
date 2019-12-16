package exception;

public class UnexpectedSymbolException extends Exception {

    public UnexpectedSymbolException() {
    }

    public UnexpectedSymbolException(String message) {
        super(message);
    }
}
