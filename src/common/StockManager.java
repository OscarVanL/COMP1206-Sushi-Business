package common;

import exceptions.*;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oscar van Leusen
 */
public class StockManager {

    //HashMap linking a dish (key) to stock (Integer).
    HashMap<Dish, StockItem> dishStock;
    //HashMap linking an ingredient (key) to stock (value). Float is used for stock as units can have decimals (eg: 1.5 Litres)
    HashMap<Ingredient, StockItem> ingredientStock;

    /**
     * Instantiates StockManager without any parameters, stocked dishes and ingredients must be later added.
     */
    public StockManager() {
        dishStock = new HashMap<>();
        ingredientStock = new HashMap<>();
    }
    public StockManager(HashMap<Dish, StockItem> dishStock, HashMap<Ingredient, StockItem> ingredientStock) {
        this.dishStock = dishStock;
        this.ingredientStock = ingredientStock;
    }

    /**
     * Used for adding a new dish, or adding more prepared dishes to the prepared dishes stock
     */
    public void addDish(Dish toAdd, int amountToAdd) {
        //There are already some of this dish prepared, so add to the number in stock
        if (dishStock.containsKey(toAdd)) {
            dishStock.get(toAdd).addStock(amountToAdd);
        } else {
            //This common.Dish isn't in our common.StockManager yet, so add it to the HashMap.
            StockItem newDish = null;
            try {
                newDish = new StockItem(toAdd, amountToAdd, 0, 0);
            } catch (InvalidStockItemException e) {
                e.printStackTrace();
            }
            dishStock.put(toAdd, newDish);
        }
    }

    public void addDish(Dish toAdd, StockItem stockData) {
        dishStock.put(toAdd, stockData);
    }

    public void removeDish(Dish toRemove) {
        dishStock.remove(toRemove);
    }

    public void addDishes(HashMap<Dish, StockItem> dishesToAdd) {
        dishStock.putAll(dishesToAdd);
    }

    public void addIngredient(Ingredient toAdd, long unitsToAdd) {
        if (ingredientStock.containsKey(toAdd)) {
            ingredientStock.get(toAdd).addStock(unitsToAdd);
        } else {
            //This common.Ingredient isn't in our common.StockManager yet, so add it to the HashMap
            StockItem newIngredient = null;
            try {
                newIngredient = new StockItem(toAdd, unitsToAdd, 0, 0);
            } catch (InvalidStockItemException e) {
                e.printStackTrace();
            }
            ingredientStock.put(toAdd, newIngredient);
        }
    }

    public void addIngredient(Ingredient toAdd, StockItem stockData) {
        ingredientStock.put(toAdd, stockData);
    }

    public void removeIngredient(Ingredient toRemove) {
        ingredientStock.remove(toRemove);
    }

    public void addIngredients(HashMap<Ingredient, StockItem> ingredientsToAdd) {
        ingredientStock.putAll(ingredientsToAdd);
    }

    public long getStockLevel(Model model) throws InvalidStockItemException {
        if (model instanceof Dish) {
            return dishStock.get(model).getStock();
        } else if (model instanceof Ingredient) {
            return ingredientStock.get(model).getStock();
        } else {
            throw new InvalidStockItemException("Attempted to get Stock levels of non-stocked Mbject (not Dish or Ingredient)");
        }
    }

    public void setStockLevel(Model model, Number stockLevel) throws InvalidStockItemException {
        if (model instanceof Dish) {
            dishStock.get(model).setStock(stockLevel);
        } else if (model instanceof Ingredient) {
            ingredientStock.get(model).setStock(stockLevel);
        } else {
            throw new InvalidStockItemException("Attempted to set Stock levels of non-stocked Model (not Dish or Ingredient)");
        }
    }

    public long getRestockThreshold(Model model) throws InvalidStockItemException {
        if (model instanceof Dish) {
            return dishStock.get(model).getRestockThreshold();
        } else if (model instanceof Ingredient) {
            return ingredientStock.get(model).getRestockThreshold();
        } else {
            throw new InvalidStockItemException("Attempted to get the restock threshold of non-stocked Model (not Dish or Ingredient)");
        }
    }

    public void setRestockThreshold(Model model, Number restockThreshold) throws InvalidStockItemException {
        if (model instanceof Dish) {
            dishStock.get(model).setRestockThreshold(restockThreshold);
        } else if (model instanceof Ingredient) {
            ingredientStock.get(model).setRestockThreshold(restockThreshold);
        } else {
            throw new InvalidStockItemException("Attempted to set restock threshold of non-stocked Model (not Dish or Ingredient)");
        }
    }

    public void setRestockAmount(Model model, Number restockAmount) throws InvalidStockItemException {
        if (model instanceof Dish) {
            dishStock.get(model).setRestockAmount(restockAmount);
        } else if (model instanceof Ingredient) {
            ingredientStock.get(model).setRestockAmount(restockAmount);
        } else {
            throw new InvalidStockItemException("Attempted to set restock amount of non-stocked Model (Not Dish or Ingredient)");
        }
    }

    public long getRestockAmount(Model model) throws InvalidStockItemException {
        if (model instanceof Dish) {
            return dishStock.get(model).getRestockAmount();
        } else if (model instanceof Ingredient) {
            return ingredientStock.get(model).getRestockAmount();
        } else {
            throw new InvalidStockItemException("Attempted to get restock amount of non-stocked Model (Not Dish or Ingredient");
        }
    }

    /**
     * Gets all Ingredient stock levels as a Map
     * @return Map of Ingredient as Key and stock as Value
     */
    public Map<Ingredient, Number> getIngredientStockLevels() {
        Map<Ingredient, Number> stocks = new HashMap<>();
        for (Map.Entry<Ingredient, StockItem> stock : ingredientStock.entrySet()) {
            stocks.put(stock.getKey(), stock.getValue().getStock());
        }
        return stocks;
    }

    /**
     * Gets all Dish stock levels as a Map
     * @return Map of Dish as Key and stock as Value
     */
    public Map<Dish, Number> getDishStockLevels() {
        Map<Dish, Number> stocks = new HashMap<>();
        for (Map.Entry<Dish, StockItem> stock : dishStock.entrySet()) {
            stocks.put(stock.getKey(), stock.getValue().getStock());
        }
        return stocks;
    }

    /**
     * Finds any dishes that are below stock levels and returns them
     * Synchronized so that it is thread safe, that is that multiple Staff won't get the same dish returned and both restock the same dish.
     * @return Dish : Dish to restock.
     */
    public synchronized Dish findDishToRestock() {
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
     * @return boolean: True if chef can make the quantity with the ingredients available, False if not.
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
