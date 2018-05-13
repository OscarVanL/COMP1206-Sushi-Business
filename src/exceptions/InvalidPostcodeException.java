package exceptions;

/**
 * @author Oscar van Leusen
 */
public class InvalidPostcodeException extends Exception {
    public InvalidPostcodeException(String errorMessage) {
        super(errorMessage);
    }
}
