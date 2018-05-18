package exceptions;

/**
 * Exception thrown if invalid Postcode is read from Config file.
 * @author Oscar van Leusen
 */
public class InvalidPostcodeException extends Exception {
    public InvalidPostcodeException(String errorMessage) {
        super(errorMessage);
    }
}
