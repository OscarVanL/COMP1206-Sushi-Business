package exceptions;

/**
 * Thrown if there is no supplier for a given Dish but one is required.
 * Thrown if invalid Supplier data is used in Configuration.
 * @author Oscar van Leusen
 */
public class InvalidSupplierException extends Exception {
    public InvalidSupplierException(String errorMessage) {
        super(errorMessage);
    }
}
