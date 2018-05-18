package exceptions;

/**
 * Thrown if there is an attempt to get a StockItem when one does not exist for that Dish / Ingredient.
 * Thrown if Configuration has invalid Stock data.
 * @author Oscar van Leusen
 */
public class InvalidStockItemException extends Exception {
    public InvalidStockItemException(String errorMessage) {
        super(errorMessage);
    }
}