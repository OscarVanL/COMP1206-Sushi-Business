package exceptions;

/**
 * Exception that is thrown if invalid Dish data is read from the Config file.
 * @author Oscar van Leusen
 */
public class InvalidDishException extends Exception {
    public InvalidDishException(String errorMessage) {
        super(errorMessage);
    }
}
