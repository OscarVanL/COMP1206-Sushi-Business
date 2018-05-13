package exceptions;

/**
 * @author Oscar van Leusen
 */
public class InvalidStockItemException extends Exception {
    public InvalidStockItemException(String errorMessage) {
        super(errorMessage);
    }
}