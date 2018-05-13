package exception;

/**
 * Thrown if supplier read in for an ingredient or dish does not match any existing suppliers.
 * @author Oscar van Leusen
 */
public class InvalidSupplierException extends Exception {
    public InvalidSupplierException(String errorMessage) {
        super(errorMessage);
    }
}
