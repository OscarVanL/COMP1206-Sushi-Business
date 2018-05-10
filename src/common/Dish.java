package common;

import common.Model;
import common.UpdateListener;

import java.util.HashMap;
import java.util.Set;

public class Dish extends Model {

    private String dishDescription;
    //Price stored as int (multiplied by 100) to avoid floating point problems (eg: £3.50 != £3.500000001)
    private int intPrice;
    private HashMap<Ingredient, Float> ingredientAmounts = new HashMap<>();

    private Dish(String dishName, String dishDescription, float price) {
        super.setName(dishName);
        this.dishDescription = dishDescription;
        //Prices can be given as floats, but they are multiplied by 100 to store them as integers. Eg: 1.50 becomes 150.
        this.intPrice = Math.round(price * 100);
    }

    /**
     * Used to add an ingredient to the dish along with the amount required
     * @param ingredient : Ingredient object to add
     * @param amount : Units of this ingredient required
     */
    private void addIngredient(Ingredient ingredient, Float amount) {
        if (ingredientAmounts.containsKey(ingredient)) {
            System.out.println("Ingredient already present in dish. Amount required updated");
            ingredientAmounts.replace(ingredient, amount);
        }
    }

    public void setPrice(float price) {
        notifyUpdate("price",(float)this.intPrice/100, price);
        this.intPrice = Math.round(price * 100);
    }

    /**
     * Gets the quantity of ingredient required for making a dish
     * @param ingredient : Ingredient to check quantity for in dish
     * @return : Quantity of ingredient required
     */
    public float getQuantity(Ingredient ingredient) {
        return ingredientAmounts.get(ingredient);
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
