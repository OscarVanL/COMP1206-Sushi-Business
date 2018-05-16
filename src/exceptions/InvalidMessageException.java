package exceptions;

/**
 * @author Oscar van Leusen
 */
public class InvalidMessageException extends Exception {
    public InvalidMessageException(String errorMessage) {
        super(errorMessage);
    }
}
