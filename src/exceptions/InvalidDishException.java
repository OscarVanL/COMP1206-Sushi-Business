package exceptions;

/**
 * @author Oscar van Leusen
 */
public class InvalidDishException extends Exception {
    public InvalidDishException(String errorMessage) {
        super(errorMessage);
    }
}
