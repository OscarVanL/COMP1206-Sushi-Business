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


    /**
     * Called when the Staff have finished making an order, removes the number of each order from the stock.
     * @param order
     */
    public void orderComplete(HashMap<Dish, Number> order) {
        for (Map.Entry<Dish, Number> orderedItem : order.entrySet()) {
            Dish dish = orderedItem.getKey();
            Integer number = orderedItem.getValue().intValue();
            dishStock.get(dish).removeStock(number);
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
        if (dish != null) {
            StockItem dishData = dishStock.get(dish);

            //Waits between 20 and 60 seconds while the cook makes the dishes
            int randomNum = ThreadLocalRandom.current().nextInt(20, 61);
            try {
                Thread.sleep(1000*randomNum);
            } catch (InterruptedException e) {
                System.out.println("Staff cooking meal interrupted");
            }



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

    public void restockIngredient(Ingredient ingredient, int flyingSpeed) {
        if (ingredient != null) {
            StockItem ingredientData = ingredientStock.get(ingredient);

            //Waits the time required for the drone to go to the supplier and back (supplier distance * 2) / speed.
            long sleepSeconds = (ingredient.getSupplier().getDistance() * 2) / flyingSpeed;
            try {
                Thread.sleep(1000*sleepSeconds);
            } catch (InterruptedException e) {
                System.out.println("Drone fetching ingredients interrupted");
            }

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

    public StockItem getStockItem(Ingredient ingredient) {
        return ingredientStock.get(ingredient);
    }

    public StockItem getStockItem(Dish dish) {
        return dishStock.get(dish);
    }
}
