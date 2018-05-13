package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oscar van Leusen
 */
public class Order extends Model implements Serializable {

    //The price of the current order as an integer (Rather than float to avoid
    private long orderPrice;
    private HashMap<Dish, Integer> basket = new HashMap<>();
    private User user;

    public Order(User user) {
        this.orderPrice = 0;
        this.user = user;
        this.name = user.getName() + "'s order.";
    }

    public void addDish(Dish dish, int quantity) {
        //If we already had this item in our basket, update the quantity in the basket.
        if (basket.containsKey(dish)) {
            notifyUpdate("dish quantity added", basket.get(dish), basket.get(dish) + quantity);
            basket.put(dish, basket.get(dish) + quantity);
        } else {
            notifyUpdate("dish added", 0, quantity);
            basket.put(dish, quantity);
        }
        calculatePrice();
    }

    /**
     * Removes a dish from the basket
     * @param dish : Dish to remove
     */
    public void removeDish(Dish dish) {
        if (basket.containsKey(dish)) {
            notifyUpdate("removed dish", dish, null);
            basket.remove(dish);
        }
        calculatePrice();
    }

    /**
     * Removes a certain quantity of dishes from the basket
     * If this means that we have 0 or fewer dishes in the basket, it is also removed from our HashMap.
     * @param dish : Dish to remove
     * @param quantity : Number to remove
     */
    public void removeDishQuantity(Dish dish, int quantity) {
        //Make sure that this Dish is already in the basket, otherwise do nothing.
        if (basket.containsKey(dish)) {
            //If the amount we want to remove makes the quantity zero or lower, we remove the dish entirely.
            if (basket.get(dish) - quantity <= 0) {
                removeDish(dish);
            } else {
                notifyUpdate("dish quantity removed", basket.get(dish), basket.get(dish) - quantity);
                basket.put(dish, basket.get(dish) - quantity);
            }
        }
        calculatePrice();
    }

    /**
     * Gets the total order price
     * @return long : Total order price for all the items ordered.
     */
    public long getOrderPrice() {
        return this.orderPrice;
    }

    /**
     * Calculates/recalculates the price of the basket, called after any modification of the basket.
     */
    public void calculatePrice() {
        long oldPrice = this.orderPrice;
        this.orderPrice = 0;
        for (Map.Entry<Dish, Integer> basketEntry : basket.entrySet()) {
            Dish dish = basketEntry.getKey();
            Integer quantity = basketEntry.getValue();
            this.orderPrice+=dish.getPrice()*quantity;
        }
        if (oldPrice != orderPrice) {
            notifyUpdate("price", oldPrice, this.orderPrice);
        }

    }

    /**
     * Gets the user's basket
     * @return HashMap containing Dishes and quantity
     */
    public HashMap<Dish, Integer> getBasket() {
        return this.basket;
    }

    /**
     * Returns a String representation of everything in the basket
     * @return : String representing contents of the order.
     */
    @Override
    public String toString() {
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append(user.getName() + "'s order containing: ");
        for (Map.Entry<Dish, Integer> basketEntry : basket.entrySet()) {
            orderDetails.append(basketEntry.getValue() + " orders of " + basketEntry.getKey().getName());
        }
        orderDetails.append(" totalling: " + this.orderPrice);
        return orderDetails.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }
}