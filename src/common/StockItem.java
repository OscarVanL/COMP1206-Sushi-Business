package common;

import exception.InvalidStockItemException;

/**
 * @author Oscar van Leusen
 */
public class StockItem {

    private boolean isDish;
    private boolean isIngredient;

    private Model stockedItem;
    private long amountStocked;
    private long restockThreshold;
    private long restockAmount;

    /**
     * Creates a common.StockItem for a given Model (common.Dish or common.Ingredient), with the current stock and restocking Threshold.
     * @param stockItem : Model object passed in, from which we use instanceof to determine if it is a common.Dish or common.Ingredient.
     * @param stock : Existing stock, passed as a long incase it is an ingredient (but if it is a dish this is casted to int)
     * @throws Exception : Exception thrown if Model object is not a common.Dish or common.Ingredient (eg: supplier)
     */
    public StockItem(Model stockItem, long stock, long restockThreshold, long restockAmount) throws InvalidStockItemException {
        this.stockedItem = stockItem;
        if (stockItem instanceof Dish) {
            this.isDish = true;
            this.isIngredient = false;
            this.amountStocked = (int) stock;
            this.restockThreshold = (int) restockThreshold;
            this.restockAmount = (int) restockAmount;
        } else if (stockItem instanceof Ingredient) {
            this.isIngredient = true;
            this.isDish = false;
            this.amountStocked = stock;
            this.restockThreshold = restockThreshold;
            this.restockAmount = restockAmount;
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
            addStock((int) stockToAdd);
        }
        amountStocked += stockToAdd;
    }

    public void addStock(int stockToAdd) {
        amountStocked += stockToAdd;
    }

    public void setStock(long stock) {
        //If it's a dish, we only allow integer levels of stock
        if (isDish) {
            setStock((int) stock);
        }
        amountStocked = stock;
    }

    public void addRestockAmount() {
        amountStocked += restockAmount;
    }

    public void setStock(int stock) {
        amountStocked = stock;
    }

    public long getStock() {
        return amountStocked;
    }

    public long getRestockThreshold() {
        return restockThreshold;
    }

    public void setRestockThreshold(long restockThreshold) {
        //If it's a dish, we only allow integer restock thresholds
        if (isDish) {
            setRestockThreshold((int) restockThreshold);
        }
        this.restockThreshold = restockThreshold;
    }

    public long getRestockAmount() {
        return restockAmount;
    }

    public void setRestockAmount(long restockAmount) {
        //If it's a dish, the restock amount must be an integer amount.
        if (isDish) {
            setRestockAmount((int) restockAmount);
        }
        this.restockAmount = restockAmount;
    }

    public void setRestockAmount(int restockAmount) {
        this.restockAmount = restockAmount;
    }

    public void setRestockThreshold(int restockThreshold) {
        this.restockThreshold = restockThreshold;
    }

    public boolean isDish() {
        return isDish;
    }

    public boolean isIngredient() {
        return isIngredient;
    }
}
