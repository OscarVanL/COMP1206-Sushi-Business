package exceptions;

/**
 * Exception that is thrown if invalid Ingredient data is read from the config file
 * @author Oscar van Leusen
 */
public class InvalidIngredientException extends Exception {
    public InvalidIngredientException(String errorMessage) { super(errorMessage); }
}
