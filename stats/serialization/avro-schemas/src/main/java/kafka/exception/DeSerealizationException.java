package kafka.exception;

public class DeSerealizationException extends RuntimeException {
    public DeSerealizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
