package exceptions;

/**
 * @author Oscar van Leusen
 */
public class InvalidUserException extends Exception {
    public InvalidUserException(String errorMessage) {
        super(errorMessage);
    }
}
