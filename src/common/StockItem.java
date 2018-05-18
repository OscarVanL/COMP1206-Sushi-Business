package common;

import exceptions.InvalidStockItemException;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class StockItem implements Serializable {

    private boolean isDish;
    private boolean isIngredient;

    private Model stockedItem;
    private Long amountStocked;
    private Long restockThreshold;
    private Long restockAmount;
    private boolean beingRestocked = false;

    /**
     * Creates a common.StockItem for a given Model (common.Dish or common.Ingredient), with the current stock and restocking Threshold.
     * @param stockItem : Model object passed in, from which we use instanceof to determine if it is a common.Dish or common.Ingredient.
     * @param stock : Existing stock, passed as a long incase it is an ingredient (but if it is a dish this is casted to int)
     * @throws Exception : Exception thrown if Model object is not a common.Dish or common.Ingredient (eg: supplier)
     */
    public StockItem(Model stockItem, long stock, Number restockThreshold, Number restockAmount) throws InvalidStockItemException {
        this.stockedItem = stockItem;
        if (stockItem instanceof Dish) {
            this.isDish = true;
            this.isIngredient = false;;
        } else if (stockItem instanceof Ingredient) {
            this.isIngredient = true;
            this.isDish = false;
        } else {
            throw new InvalidStockItemException("Non-valid stock item (not common.Dish or common.Ingredient) was used");
        }
        this.amountStocked = stock;
        this.restockThreshold = restockThreshold.longValue();
        this.restockAmount = restockAmount.longValue();
    }

    /**
     * Gets the Dish or Ingredient that this StockItem is recording stock for.
     * @return Dish or Ingredient object
     */
    public Model getStockedItem() {
        return this.stockedItem;
    }

    /**
     * Adds stock for this Dish/Ingredient
     * @param stockToAdd : Long amount of stock to add.
     */
    public void addStock(long stockToAdd) {
        amountStocked += stockToAdd;
    }

    /**
     * Removes a number of stock for this Dish/Ingredient
     * @param stockToRemove : Long amount of stock to remove
     */
    public void removeStock(long stockToRemove) {
        if (isDish) {
            amountStocked -= (int) stockToRemove;
        } else {
            amountStocked -= stockToRemove;
        }
    }

    /**
     * Sets the number of stock to a specific amount for this Dish/Ingredient
     * @param stock : Number of stock to set to (later casted to long)
     */
    public void setStock(Number stock) {
        amountStocked = stock.longValue();
    }

    /**
     * Gets the stock for the stocked Dish/Ingredient
     * @return :
     */
    public Long getStock() {
        return amountStocked;
    }

    /**
     * Gets the amount that the Stock must fall below before it is restocked
     * @return : Long amount the stock must fall below before it is restocked
     */
    public Long getRestockThreshold() {
        return restockThreshold;
    }

    /**
     * Sets the threshold for which the Stock must fall below before it is restocked
     * @param restockThreshold : Amount the stock must fall below before it is restocked.
     */
    public void setRestockThreshold(Number restockThreshold) {
        this.restockThreshold = restockThreshold.longValue();
    }

    /**
     * Gets the amount to restock by when the stock falls below the Restock Threshold
     * @return : Long amount to restock by.
     */
    public Long getRestockAmount() {
        return restockAmount;
    }

    /**
     * Sets the amount to restock the Dish/Ingredient by when it falls below the Restock Threshold.
     * @param restockAmount : Amount to restock by
     */
    public void setRestockAmount(Number restockAmount) {
        this.restockAmount = restockAmount.longValue();
    }

    /**
     * Returns whether the StockItem is representing a Dish
     * @return : True if it is, False if it is not
     */
    public boolean isDish() {
        return isDish;
    }

    /**
     * Returns whether the StockItem is representing an Ingredient
     * @return : True if it is, False if it is not.
     */
    public boolean isIngredient() {
        return isIngredient;
    }

    /**
     * Sets whether this Ingredient/Dish is currently being restocked
     * @param beingRestocked : New state of whether dish is being restocked
     */
    public void setBeingRestocked(boolean beingRestocked) {
        this.beingRestocked = beingRestocked;
    }

    /**
     * Returns whether this stockitem is currently being restocked.
     * @return : True - is being restocked. False - is not being restocked
     */
    public boolean beingRestocked() {
        return this.beingRestocked;
    }
}
