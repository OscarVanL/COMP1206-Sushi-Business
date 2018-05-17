package common;

import exceptions.InvalidStockItemException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Oscar van Leusen
 */
public class Dish extends Model implements Serializable {

    private String dishName;
    private String dishDescription;
    private Double price;
    private StockManager stockManager;
    private HashMap<Ingredient, Long> ingredientAmounts = new HashMap<>();

    public Dish(String dishName, String dishDescription, Number price, StockManager manager) {
        this.dishName = dishName;
        this.dishDescription = dishDescription;
        this.price = price.doubleValue();
        this.stockManager = manager;
    }

    /**
     * Used to add an ingredient to the dish along with the amount required (if ingredient already exists, amount is updated)
     * @param ingredient : Ingredient object to add
     * @param amount : Units of this ingredient required
     */
    public void addIngredient(Ingredient ingredient, Number amount) {
        if (ingredientAmounts.containsKey(ingredient)) {
            System.out.println("Ingredient already present in dish. Amount required updated");
            setQuantity(ingredient, amount.longValue());
        } else {
            notifyUpdate("Ingredient added", null, ingredient);
            ingredientAmounts.put(ingredient, amount.longValue());
        }
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredientAmounts.remove(ingredient);
    }

    /**
     * Used to change the amount of an ingredient required
     * @param ingredient : Ingredient to update
     * @param newQuantity : New quantity of ingredient required
     */
    public void setQuantity(Ingredient ingredient, Long newQuantity) {
        if (ingredientAmounts.containsKey(ingredient)) {
        notifyUpdate("Ingredient quantity updated", ingredientAmounts.get(ingredient), newQuantity);
            ingredientAmounts.replace(ingredient, newQuantity);
        }
    }

    /**
     * Gets the price of one of this dish
     * @return long : Price of one of this dish
     */
    public Double dishPrice() {
        return this.price;
    }

    /**
     * Assigns the price of the Dish
     * @param price : Price of the dish as a float.
     */
    public void setPrice(Double price) {
        notifyUpdate("price",this.price, price);
        this.price = price;
    }

    /**
     * Gets the quantity of ingredient required for making a dish
     * @param ingredient : Ingredient to check quantity for in dish
     * @return : Quantity of ingredient required
     */
    public long getQuantity(Ingredient ingredient) {
        return ingredientAmounts.get(ingredient);
    }

    public String getDishDescription() {
        return this.dishDescription;
    }

    public boolean containsIngredient(Ingredient ingredient) {
        if (ingredientAmounts.containsKey(ingredient)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets all the ingredients in the recipe as a set
     * @return Set of Ingredient objects
     */
    public Set<Ingredient> getDishIngredients() {
        return ingredientAmounts.keySet();
    }

    public void setRecipe(Map<Ingredient, Number> newRecipe) {
        HashMap<Ingredient, Long> newRecipeCasted = new HashMap<>();
        for (Map.Entry<Ingredient, Number> newRecipeIngredient : newRecipe.entrySet()) {
            newRecipeCasted.put(newRecipeIngredient.getKey(), newRecipeIngredient.getValue().longValue());
        }
        ingredientAmounts = newRecipeCasted;
    }

    public Map<Ingredient, Number> getRecipe() {
        Map<Ingredient, Number> recipe = new HashMap<>();
        for (Map.Entry<Ingredient, Long> dishIngredients : ingredientAmounts.entrySet()) {
            recipe.put(dishIngredients.getKey(), dishIngredients.getValue());
        }
        return recipe;
    }

    public Long getRestockThreshold() {
        try {
            return stockManager.getRestockThreshold(this);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long getRestockAmount() {
        try {
            return stockManager.getRestockAmount(this);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getName() {
        return this.dishName;
    }
}
