package exceptions;

/**
 * Thrown if invalid user data is read from the Config file.
 * @author Oscar van Leusen
 */
public class InvalidUserException extends Exception {
    public InvalidUserException(String errorMessage) {
        super(errorMessage);
    }
}
