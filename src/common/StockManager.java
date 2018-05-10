package common;

import common.Dish;
import common.Ingredient;
import common.StockItem;

import java.util.HashMap;

public class StockManager {

    //HashMap linking a dish (key) to stock (Integer).
    HashMap<Dish, StockItem> dishStock = new HashMap<>();
    //HashMap linking an ingredient (key) to stock (value). Float is used for stock as units can have decimals (eg: 1.5 Litres)
    HashMap<Ingredient, StockItem> ingredientStock = new HashMap<>();

    /**
     * Used for adding a new dish, or adding more prepared dishes to the prepared dishes stock
     */
    public void addDish(Dish toAdd, int amountToAdd) throws Exception {
        //There are already some of this dish prepared, so add to the number in stock
        if (dishStock.containsKey(toAdd)) {
            dishStock.get(toAdd).addStock(amountToAdd);
        } else {
            //This common.Dish isn't in our common.StockManager yet, so add it to the HashMap.
            StockItem newDish = new StockItem(toAdd, amountToAdd);
            dishStock.put(toAdd, newDish);
        }
    }

    public void addIngredient(Ingredient toAdd, float unitsToAdd) throws Exception {
        if (ingredientStock.containsKey(toAdd)) {
            ingredientStock.get(toAdd).addStock(unitsToAdd);
        } else {
            //This common.Ingredient isn't in our common.StockManager yet, so add it to the HashMap
            StockItem newIngredient = new StockItem(toAdd, unitsToAdd);
            ingredientStock.put(toAdd, newIngredient);
        }
    }

    public Dish findDishToRestock() {
        //Iterate through every dish we need to stock.
        for (Dish dish : dishStock.keySet()) {
            StockItem stock = dishStock.get(dish);
            //If there are items where the restock threshold exceeds the number of prepared dishes, we need to make more.
            if (stock.getRestockThreshold() > stock.getStock()) {
                //If there are sufficient ingredients to make the restock amount
                if (canMakeMinQuantity(dish)) {
                    //Return the dish (to the staff to be made).
                    return dish;
                }
            }
        }
        //If there are no dishes (with sufficient ingredient stock) to make, return null.
        return null;
    }

    /**
     * Checks if a chef is able to make the minimum quantity of a dish based on the amount of ingredients in stock
     * @param dish : Dish to check status for.
     * @return
     */
    public boolean canMakeMinQuantity(Dish dish) {
        int dishesToMake = (int) dishStock.get(dish).getRestockAmount();
        boolean canMake = true;
        //Iterate through every ingredient required to make the dish
        for (Ingredient ingredient : dish.getDishIngredients()) {
            //If there is not enough stock of ingredient required to make the dishes, return false.
            if (ingredientStock.get(ingredient).getStock() < (dish.getQuantity(ingredient) * dishesToMake)) {
                System.out.println("There is not enough " + ingredient.getName() + " in stock to make " + dishesToMake + " dishes.");
                canMake = false;
            }
        }
        //If the flag is never set to false, we are able to make the dish.
        return canMake;
    }

    /**
     * Restocks the dish if it falls below the restock threshold.
     * @param dish : Dish to restock
     */
    public void restockDish(Dish dish) {
        StockItem dishData = dishStock.get(dish);
        //If the restock amount is 0, it will be restocked back purely to the threshold.
        if (dishData.getRestockAmount() == 0) {
            dishData.setStock(dishData.getRestockThreshold());
        } else {
            //Once the stock of an ingredient or dish falls below the restock threshold, then they are restocked up until the stock reaches the restock threshold + the restock amount.
            dishData.setStock(dishData.getRestockThreshold() + dishData.getRestockAmount());
        }
    }
}
