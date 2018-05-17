package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static common.Order.OrderState.*;


/**
 * @author Oscar van Leusen
 */
public class Order extends Model implements Serializable {
    public enum OrderState {
        BASKET, PREPARING, PREPARED, DELIVERING, COMPLETE, CANCELLED
    }

    //The price of the current order as an integer (Rather than float to avoid
    private Double orderPrice;
    private HashMap<Dish, Number> basket = new HashMap<>();
    private User user;
    private int orderNumber;
    private OrderState state;

    public Order(User user, int orderNumber) {
        this.orderPrice = 0.00;
        this.user = user;
        this.name = user.getName() + "'s order.";
        this.orderNumber = orderNumber;
        this.state = BASKET;
    }

    public void addDish(Dish dish, int quantity) {
        //If we already had this item in our basket, update the quantity in the basket.
        if (basket.containsKey(dish)) {
            notifyUpdate("dish quantity added", basket.get(dish), basket.get(dish).intValue() + quantity);
            basket.replace(dish, basket.get(dish).intValue() + quantity);
        } else {
            notifyUpdate("dish added", 0, quantity);
            basket.put(dish, quantity);
        }
        calculatePrice();
    }

    public void addDishes(HashMap<Dish, Number> order) {
        basket.putAll(order);
    }

    public void updateDishQuantity(Dish dish, int newQuantity) {
        if (basket.containsKey(dish)) {
            notifyUpdate("dish quantity in basket updated", basket.get(dish), newQuantity);
            basket.replace(dish, newQuantity);
        } else {
            notifyUpdate("dish added", 0, newQuantity);
            basket.put(dish, newQuantity);
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
            calculatePrice();
        }
    }

    public void clear() {
        //Use of .clone() is usually discouraged because when used incorrectly it can cause problems,
        //but in this case we just want to copy the basket object by value and not reference before clearing it.
        //So the usual reasons for avoiding it do not apply.
        HashMap<Dish, Integer> oldBasket = (HashMap<Dish, Integer>) basket.clone();
        basket.clear();
        notifyUpdate("cleared basket", oldBasket, basket);
    }

    public boolean containsDish(Dish dish) {
        if (basket.containsKey(dish)) {
            return true;
        } else {
            return false;
        }
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
            if (basket.get(dish).intValue() - quantity <= 0) {
                removeDish(dish);
            } else {
                notifyUpdate("dish quantity removed", basket.get(dish), basket.get(dish).intValue() - quantity);
                basket.put(dish, basket.get(dish).intValue() - quantity);
            }
        }
        calculatePrice();
    }

    /**
     * Gets the total order price
     * @return long : Total order price for all the items ordered.
     */
    public Double orderPrice() {
        calculatePrice();
        return this.orderPrice;
    }

    /**
     * Calculates/recalculates the price of the basket, called after any modification of the basket.
     */
    public void calculatePrice() {
        Double oldPrice = this.orderPrice;
        this.orderPrice = 0.00;
        for (Map.Entry<Dish, Number> basketEntry : basket.entrySet()) {
            Dish dish = basketEntry.getKey();
            Integer quantity = basketEntry.getValue().intValue();
            this.orderPrice+=dish.dishPrice()*quantity;
        }
        if (oldPrice != orderPrice) {
            notifyUpdate("price", oldPrice, this.orderPrice);
        }

    }

    /**
     * Gets the user's basket
     * @return HashMap containing Dishes and quantity
     */
    public HashMap<Dish, Number> getBasket() {
        return this.basket;
    }

    public void setBasket(HashMap<Dish, Number> newOrderData) {
        this.basket.clear();
        this.basket = newOrderData;
    }

    public void cancelOrder() {
        setOrderState(OrderState.CANCELLED);
    }

    public synchronized void deliverOrder(int flyingSpeed) throws InterruptedException {
        float sleepSeconds = ((float) user.getPostcode().getDistance() * 200) / flyingSpeed;
        Thread.sleep((long) (1000*sleepSeconds));
        setOrderState(OrderState.COMPLETE);
        System.out.println("Delivered order in " + sleepSeconds + " seconds.");
    }

    /**
     * Returns a String representation of everything in the basket
     * @return : String representing contents of the order.
     */
    @Override
    public String toString() {
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append(user.getName() + "'s order:");
        boolean firstDish = true;
        for (Map.Entry<Dish, Number> basketEntry : basket.entrySet()) {
            if (firstDish) {
                orderDetails.append(" " + basketEntry.getValue() + " x " + basketEntry.getKey().getName());
                firstDish = false;
            } else {
                orderDetails.append(", " + basketEntry.getValue() + " x " + basketEntry.getKey().getName());
            }

        }
        return orderDetails.toString();
    }

    /**
     * Returns the user whose order this is
     * @return User : User whose order this is
     */
    public User getUser() {
        return this.user;
    }

    public void setOrderState(OrderState state) {
        notifyUpdate("state", this.state, state);
        this.state = state;

    }

    public int getUserOrderNum() {
        return this.orderNumber;
    }

    public OrderState getOrderState() {
        return this.state;
    }

    @Override
    public String getName() {
        return this.toString();
    }
}