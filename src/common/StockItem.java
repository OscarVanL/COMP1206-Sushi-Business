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
    private long amountStocked;
    private long restockThreshold;
    private long restockAmount;
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
            this.isIngredient = false;
            this.amountStocked = (int) stock;
            this.restockThreshold = restockThreshold.intValue();
            this.restockAmount = restockAmount.intValue();
        } else if (stockItem instanceof Ingredient) {
            this.isIngredient = true;
            this.isDish = false;
            this.amountStocked = stock;
            this.restockThreshold = restockThreshold.longValue();
            this.restockAmount = restockAmount.longValue();
        } else {
            throw new InvalidStockItemException("Non-valid stock item (not common.Dish or common.Ingredient) was used");
        }
    }

    public Model getStockedItem() {
        return this.stockedItem;
    }

    public void addStock(long stockToAdd) {
        //If it's a dish, we only allow integer levels of stock
        if (isDish) {
            amountStocked += (int) stockToAdd;
        } else {
            //For ingredients we allow partial units.
            amountStocked += stockToAdd;
        }
    }

    public void removeStock(long stockToRemove) {
        if (isDish) {
            amountStocked -= (int) stockToRemove;
        } else {
            amountStocked -= stockToRemove;
        }
    }

    public void addRestockAmount() {
        amountStocked += restockAmount;
    }

    public void setStock(Number stock) {
        //If it's a dish, we only allow integer levels of stock
        if (isDish) {
            amountStocked = stock.intValue();
        }
        amountStocked = stock.longValue();
    }

    public long getStock() {
        return amountStocked;
    }

    public long getRestockThreshold() {
        return restockThreshold;
    }

    public void setRestockThreshold(Number restockThreshold) {
        //If it's a dish, we only allow integer restock thresholds
        if (isDish) {
            this.restockThreshold = restockThreshold.intValue();
        }
        this.restockThreshold = restockThreshold.longValue();
    }

    public long getRestockAmount() {
        return restockAmount;
    }

    public void setRestockAmount(Number restockAmount) {
        //If it's a dish, the restock amount must be an integer amount.
        if (isDish) {
            this.restockAmount = restockAmount.intValue();
        }
        this.restockAmount = restockAmount.longValue();
    }

    public boolean isDish() {
        return isDish;
    }

    public boolean isIngredient() {
        return isIngredient;
    }

    public void setBeingRestocked(boolean beingRestocked) {
        this.beingRestocked = beingRestocked;
    }

    public boolean beingRestocked() {
        return this.beingRestocked;
    }
}
