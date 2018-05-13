package exception;

/**
 * @author Oscar van Leusen
 */
public class InvalidIngredientException extends Exception {
    public InvalidIngredientException(String errorMessage) {
        super(errorMessage);
    }
}
