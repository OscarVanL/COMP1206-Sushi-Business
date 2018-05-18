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

    /**
     * Adds a dish (and quantity of dish) to the order
     * @param dish : Dish to add to the order
     * @param quantity : Number of dishes to add
     */
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

    /**
     * Adds multiple dishes an quantities to this this order.
     * @param order : HashMap containing Dishes and number of dishes to add.
     */
    public void addDishes(HashMap<Dish, Number> order) {
        basket.putAll(order);
        calculatePrice();
        notifyUpdate();
    }

    /**
     * Updates the amount of a dish in the basket
     * @param dish : Dish to update
     * @param newQuantity : New amount of this dish to have in the basket.
     */
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

    /**
     * Clears the contents of the order/basket.
     */
    public void clear() {
        basket.clear();
        notifyUpdate();
    }

    /**
     * Checks if a Dish is included in an order
     * @param dish : Dish to check
     * @return True if dish is present. False if dish is not present.s
     */
    public boolean containsDish(Dish dish) {
        return basket.containsKey(dish);
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
        if (!oldPrice.equals(orderPrice)) {
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

    /**
     * Updates the order's OrderState to indicate that it has been cancelled.
     */
    public void cancelOrder() {
        setOrderState(OrderState.CANCELLED);
    }

    /**
     * Called by the Drone to deliver the order. Delays the drone's thread for the amount of time it takes to deliver the order
     * @param flyingSpeed : Speed that the drone flies at
     * @throws InterruptedException : If the drone's delivery is interrupted.
     */
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
        orderDetails.append(user.getName()).append("'s order:");
        boolean firstDish = true;
        for (Map.Entry<Dish, Number> basketEntry : basket.entrySet()) {
            if (firstDish) {
                orderDetails.append(" ").append(basketEntry.getValue()).append(" x ").append(basketEntry.getKey().getName());
                firstDish = false;
            } else {
                orderDetails.append(", ").append(basketEntry.getValue()).append(" x ").append(basketEntry.getKey().getName());
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

    /**
     * Sets the state of the Order to an OrderState enum value
     * @param state : New state of the order
     */
    public void setOrderState(OrderState state) {
        notifyUpdate("state", this.state, state);
        this.state = state;

    }

    /**
     * Gets the order number for the given user. This is only unique to that user, not overall.
     * Eg: If I order a dish, then another person orders a dish, there will be two orders with orderNumber=0.
     * @return : Integer Order Number for that user's orders.
     */
    public int getUserOrderNum() {
        return this.orderNumber;
    }

    /**
     * Gets the state of the Order represented as OrderState enum value.
     * @return : OrderState enum value
     */
    public OrderState getOrderState() {
        return this.state;
    }

    /**
     * Gets a string representation of the Order
     * @return : String of order's content
     */
    @Override
    public String getName() {
        return this.toString();
    }
}