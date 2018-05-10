package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class Dish extends Model implements Serializable {

    private String dishDescription;
    private long price;
    private HashMap<Ingredient, Float> ingredientAmounts = new HashMap<>();

    private Dish(String dishName, String dishDescription, long price) {
        super.setName(dishName);
        this.dishDescription = dishDescription;
        this.price = price;
    }

    /**
     * Used to add an ingredient to the dish along with the amount required (if ingredient already exists, amount is updated)
     * @param ingredient : Ingredient object to add
     * @param amount : Units of this ingredient required
     */
    private void addIngredient(Ingredient ingredient, Float amount) {
        if (ingredientAmounts.containsKey(ingredient)) {
            System.out.println("Ingredient already present in dish. Amount required updated");
            setQuantity(ingredient, amount);
        } else {
            notifyUpdate("Ingredient added", null, ingredient);
            ingredientAmounts.put(ingredient, amount);
        }
    }

    /**
     * Used to change the amount of an ingredient required
     * @param ingredient : Ingredient to update
     * @param newQuantity : New quantity of ingredient required
     */
    public void setQuantity(Ingredient ingredient, Float newQuantity) {
        if (ingredientAmounts.containsKey(ingredient)) {
        notifyUpdate("Ingredient quantity updated", ingredientAmounts.get(ingredient), newQuantity);
            ingredientAmounts.replace(ingredient, newQuantity);
        }
    }

    /**
     * Gets the price of one of this dish
     * @return long : Price of one of this dish
     */
    public long getPrice() {
        return this.price;
    }

    /**
     * Assigns the price of the Dish
     * @param price : Price of the dish as a float.
     */
    public void setPrice(long price) {
        notifyUpdate("price",this.price, price);
        this.price = price;
    }

    /**
     * Gets the quantity of ingredient required for making a dish
     * @param ingredient : Ingredient to check quantity for in dish
     * @return : Quantity of ingredient required
     */
    public float getQuantity(Ingredient ingredient) {
        return ingredientAmounts.get(ingredient);
    }

    public String getDishDescription() {
        return this.dishDescription;
    }

    /**
     * Gets all the ingredients in the recipe as a set
     * @return Set of Ingredient objects
     */
    public Set<Ingredient> getDishIngredients() {
        return ingredientAmounts.keySet();
    }

    @Override
    public String getName() {
        return super.name;
    }
}
