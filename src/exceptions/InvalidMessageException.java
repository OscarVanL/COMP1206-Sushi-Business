package exceptions;

/**
 * Exception thrown if user tries to run comms.sendMessage() for non-Message object
 * @author Oscar van Leusen
 */
public class InvalidMessageException extends Exception {
    public InvalidMessageException(String errorMessage) {
        super(errorMessage);
    }
}
