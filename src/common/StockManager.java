package common;

import exceptions.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Oscar van Leusen
 */
public class StockManager implements Serializable {

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

    /**
     * Used for adding a new dish, or adding more prepared dishes to the prepared dishes stock
     * @param toAdd : Dish to add to the StockManager
     * @param amountToAdd : Amount of the Dish is already prepared.
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

    /**
     * Used for adding a new dish to the prepared dish stock.
     * @param toAdd : Dish to add
     * @param stockData : StockItem for this dish
     */
    public void addDish(Dish toAdd, StockItem stockData) {
        dishStock.put(toAdd, stockData);
    }

    /**
     * Used for removing a dish from the prepared dishes stock.
     * @param toRemove : Dish to remove
     */
    public void removeDish(Dish toRemove) {
        dishStock.remove(toRemove);
    }

    /**
     * Used to add many dishes and StockItems at once to the prepared dish stock
     * @param dishesToAdd : Dishes to add to the prepared dish stock.
     */
    public void addDishes(HashMap<Dish, StockItem> dishesToAdd) {
        dishStock.putAll(dishesToAdd);
    }

    /**
     * Used to add an ingredient to the ingredient stock manager
     * @param toAdd : Ingredient to add
     * @param unitsToAdd : Units stocked
     */
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

    /**
     * Used to add an ingredient and the StockItem to the ingredient stock manager
     * @param toAdd : Ingredient to add
     * @param stockData : StockItem for this ingredient
     */
    public void addIngredient(Ingredient toAdd, StockItem stockData) {
        ingredientStock.put(toAdd, stockData);
    }

    /**
     * Removes an ingredient from the ingredient stock manager
     * @param toRemove : Ingredient to remove
     */
    public void removeIngredient(Ingredient toRemove) {
        ingredientStock.remove(toRemove);
    }

    /**
     * Used to add many ingredients at once to the ingredient stock manager
     * @param ingredientsToAdd : Map containing Ingredient and StockItem to add to the stock manager.
     */
    public void addIngredients(HashMap<Ingredient, StockItem> ingredientsToAdd) {
        ingredientStock.putAll(ingredientsToAdd);
    }

    /**
     * Gets the stock levels for a Dish or Ingredient
     * @param model : Dish or ingredient to check
     * @return : Long value of stock held on this Dish or Ingredient
     * @throws InvalidStockItemException : If a model that is not Dish or Ingredient is passed in.
     */
    public Long getStockLevel(Model model) throws InvalidStockItemException {
        if (model instanceof Dish) {
            return dishStock.get(model).getStock();
        } else if (model instanceof Ingredient) {
            return ingredientStock.get(model).getStock();
        } else {
            throw new InvalidStockItemException("Attempted to get Stock levels of non-stocked Mbject (not Dish or Ingredient)");
        }
    }

    /**
     * Sets the stock level for a given Dish or Ingredient
     * @param model : Dish or Ingredient to update
     * @param stockLevel : New stock level of this dish/ingredient
     * @throws InvalidStockItemException : If a model that is not Dish or Ingredient is passed in.
     */
    public void setStockLevel(Model model, Number stockLevel) throws InvalidStockItemException {
        if (model instanceof Dish) {
            dishStock.get(model).setStock(stockLevel);
        } else if (model instanceof Ingredient) {
            ingredientStock.get(model).setStock(stockLevel);
        } else {
            throw new InvalidStockItemException("Attempted to set Stock levels of non-stocked Model (not Dish or Ingredient)");
        }
    }


    /**
     * Called when the Staff have finished making an order, removes the number of each order from the stock.
     * @param order Completed order
     */
    public void orderComplete(HashMap<Dish, Number> order) {
        for (Map.Entry<Dish, Number> orderedItem : order.entrySet()) {
            Dish dish = orderedItem.getKey();
            Integer number = orderedItem.getValue().intValue();
            dishStock.get(dish).removeStock(number);
        }
    }

    /**
     * Gets the threshold that the stock must reach before it is restocked
     * @param model : Dish or Ingredient to check
     * @return : Long value of the amount of stock
     * @throws InvalidStockItemException : Thrown if a Model that is not Dish or Ingredient is passed in
     */
    public Long getRestockThreshold(Model model) throws InvalidStockItemException {
        if (model instanceof Dish) {
            return dishStock.get(model).getRestockThreshold();
        } else if (model instanceof Ingredient) {
            return ingredientStock.get(model).getRestockThreshold();
        } else {
            throw new InvalidStockItemException("Attempted to get the restock threshold of non-stocked Model (not Dish or Ingredient)");
        }
    }

    /**
     * Sets the threshold the stock must reach before it is restocked
     * @param model : Dish or Ingredient to check
     * @param restockThreshold : Level at which to restock
     * @throws InvalidStockItemException : Thrown if a Model that is not Dish or Ingredient is passed in.
     */
    public void setRestockThreshold(Model model, Number restockThreshold) throws InvalidStockItemException {
        if (model instanceof Dish) {
            dishStock.get(model).setRestockThreshold(restockThreshold);
        } else if (model instanceof Ingredient) {
            ingredientStock.get(model).setRestockThreshold(restockThreshold);
        } else {
            throw new InvalidStockItemException("Attempted to set restock threshold of non-stocked Model (not Dish or Ingredient)");
        }
    }

    /**
     * Sets the amount to restock by
     * @param model : Dish or Ingredient to restock
     * @param restockAmount : Amount to restock by
     * @throws InvalidStockItemException : Thrown if a model that is not Dish or Ingredient is passed in
     */
    public void setRestockAmount(Model model, Number restockAmount) throws InvalidStockItemException {
        if (model instanceof Dish) {
            dishStock.get(model).setRestockAmount(restockAmount);
        } else if (model instanceof Ingredient) {
            ingredientStock.get(model).setRestockAmount(restockAmount);
        } else {
            throw new InvalidStockItemException("Attempted to set restock amount of non-stocked Model (Not Dish or Ingredient)");
        }
    }

    /**
     * Sets the amount to restock by
     * @param model : Dish or Ingredient to restock
     * @return : Amount to restock by
     * @throws InvalidStockItemException : Thrown if a model that is not Dish or Ingredient is passed in
     */
    public Long getRestockAmount(Model model) throws InvalidStockItemException {
        if (model instanceof Dish) {
            return dishStock.get(model).getRestockAmount();
        } else if (model instanceof Ingredient) {
            return ingredientStock.get(model).getRestockAmount();
        } else {
            throw new InvalidStockItemException("Attempted to get restock amount of non-stocked Model (Not Dish or Ingredient");
        }
    }

    /**
     * Gets a list of StockItem representing all items of stock
     * @return : List of all StockItems
     */
    public List<StockItem> getStock() {
        List<StockItem> allStock = new ArrayList<>();
        allStock.addAll(dishStock.values());
        allStock.addAll(ingredientStock.values());
        return allStock;
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
     * Finds any ingredients that are below stock levels and returns them
     * Synchronized so that it is thread safe, that is that multiple Drones won't get the same ingredient returned and both restock the same ingredient.
     * @return Ingredient : Ingredient to restock.
     */
    public synchronized Ingredient findIngredientToRestock() {
        Ingredient toReturn = null;
        //Iterate through every Ingredient we need to keep stocked above restock threshold.
        for (Ingredient ingredient : ingredientStock.keySet()) {
            StockItem stock = ingredientStock.get(ingredient);

            //If there are items where the restock threshold exceeds the number in stock, we need to get more.
            if (stock.getRestockThreshold() >= stock.getStock() && !stock.beingRestocked()) {
                stock.setBeingRestocked(true);
                toReturn = ingredient;
                break;
            }
        }

        //If there are no ingredients to restock, return null.
        return toReturn;
    }

    /**
     * Checks if a chef is able to make the minimum quantity of a dish based on the amount of ingredients in stock
     * @param dish : Dish to check status for.
     * @return boolean: True if chef can make the quantity with the ingredients available, False if not.
     */
    public boolean canMakeMinQuantity(Dish dish) {
        int dishesToMake = dishStock.get(dish).getRestockAmount().intValue();
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
    public void restockDish(Dish dish) throws InterruptedException {
        if (dish != null) {
            StockItem dishData = dishStock.get(dish);

            //Waits between 20 and 60 seconds while the cook makes the dishes
            int randomNum = ThreadLocalRandom.current().nextInt(20, 61);
            Thread.sleep(1000*randomNum);

            //If the restock amount is 0, it will be restocked back purely to the threshold.
            //This can cause problems if we get an order that has a quantity greater than the restockThreshold
            if (dishData.getRestockAmount() == 0) {
                dishData.setStock(dishData.getRestockThreshold());
            } else {
                //Once the stock of a dish falls below the restock threshold, they are restocked adding the restock amount to existing stock.
                dishData.setStock(dishData.getStock() + dishData.getRestockAmount());
            }

            //Then deduct the ingredients required to make the dish
            for (Ingredient ingredient : dish.getDishIngredients()) {
                long amountUsed = dishData.getRestockAmount() * dish.getQuantity(ingredient);
                ingredientStock.get(ingredient).removeStock(amountUsed);
            }

            dishStock.get(dish).setBeingRestocked(false);
        }
    }

    /**
     * Used by the Drones to restock an ingredient. Holds the drone's thread by the amount it takes to fly to the supplier and fetch the ingredients
     * @param ingredient : Ingredient to restock
     * @param flyingSpeed : Speed the drone flies at
     * @throws InterruptedException : Thrown if the drone's trip is interrupted
     */
    public void restockIngredient(Ingredient ingredient, int flyingSpeed) throws InterruptedException {
        if (ingredient != null) {
            StockItem ingredientData = ingredientStock.get(ingredient);

            //Waits the time required for the drone to go to the supplier and back (supplier distance * 2) / speed.
            long sleepSeconds = (ingredient.getSupplier().getDistance() * 2) / flyingSpeed;
            Thread.sleep(1000*sleepSeconds);

            //If the restock amount is 0, it will be restocked back purely to the threshold.
            if (ingredientData.getRestockAmount() == 0) {
                ingredientData.setStock(ingredientData.getRestockThreshold());
            } else {
                //Once the stock of an ingredient falls below the restock threshold, they are restocked up to restock threshold + restock amount.
                ingredientData.setStock(ingredientData.getRestockThreshold() + ingredientData.getRestockAmount());
            }
            ingredientStock.get(ingredient).setBeingRestocked(false);
        }
    }

    /**
     * Gets the StockItem for a given Ingredient
     * @param ingredient : Ingredient to retrieve StockItem for
     * @return : StockItem for the ingredient
     */
    public StockItem getStockItem(Ingredient ingredient) {
        return ingredientStock.get(ingredient);
    }

    /**
     * Gets the StockItem for a given Dish
     * @param dish : Dish to retrieve StockItem for
     * @return : StockItem for the dish
     */
    public StockItem getStockItem(Dish dish) {
        return dishStock.get(dish);
    }
}
