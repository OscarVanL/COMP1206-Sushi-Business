package common;

import common.Dish;
import common.Ingredient;
import common.Model;

public class StockItem {

    private boolean isDish;
    private boolean isIngredient;

    private float amountStocked = 0;
    private float restockThreshold = 0;
    private float restockAmount = 0;

    /**
     * Creates a common.StockItem for a given Model (common.Dish or common.Ingredient), with the current stock and restocking Threshold.
     * @param stockItem : Model object passed in, from which we use instanceof to determine if it is a common.Dish or common.Ingredient.
     * @param stock : Existing stock, passed as a float incase it is an ingredient (but if it is a dish this is casted to int)
     * @throws Exception : Exception thrown if Model object is not a common.Dish or common.Ingredient (eg: supplier)
     */
    public StockItem(Model stockItem, float stock) throws Exception {
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
            throw new Exception("Non-valid stock item (not common.Dish or common.Ingredient) was used");
        }
    }

    public void addStock(float stockToAdd) {
        //If it's a dish, we only allow integer levels of stock
        if (isDish) {
            addStock((int) stockToAdd);
        }
        amountStocked += stockToAdd;
    }

    public void addStock(int stockToAdd) {
        amountStocked += stockToAdd;
    }

    public void setStock(float stock) {
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

    public float getStock() {
        return amountStocked;
    }

    public float getRestockThreshold() {
        return restockThreshold;
    }

    public void setRestockThreshold(float restockThreshold) {
        //If it's a dish, we only allow integer restock thresholds
        if (isDish) {
            setRestockThreshold((int) restockThreshold);
        }
        this.restockThreshold = restockThreshold;
    }

    public float getRestockAmount() {
        return restockAmount;
    }

    public void setRestockAmount(float restockAmount) {
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
