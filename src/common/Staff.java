package common;

import exceptions.InvalidStockItemException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oscar van Leusen
 */
public class Staff extends Model implements Runnable {
    public enum StaffState {
        IDLE, COOKING
    }

    private volatile boolean threadRunning = true;
    private String staffName;
    private StaffState jobState;
    private StockManager stockManager;
    private ArrayList<Order> orders;
    private String currentlyMaking = "";

    public Staff(String staffName, StockManager stockManager, ArrayList<Order> orders) {
        this.staffName = staffName;
        this.stockManager = stockManager;
        this.orders = orders;
    }

    /**
     * Performs Thread-based Staff operations including:
     * 1. Finding dishes that fall below the restock threshold and restocking them
     * 2. Checking if there are enough dishes to together complete an order
     */
    @Override
    public void run() {
        Dish toRestock;
        while(threadRunning) {
            //Finds any dishes that need to be restocked (returns null if there are none)
            toRestock = findDishToRestock();
            if (toRestock != null) {
                jobState = StaffState.COOKING;
                //Adds the restock amount to the stock for this dish.
                currentlyMaking = toRestock.getName();
                try {
                    stockManager.restockDish(toRestock);
                } catch (InterruptedException e) {
                    break;
                }
                currentlyMaking = "";
                jobState = StaffState.IDLE;
            }

            checkOrderComplete();

            //Waits another 10 seconds before checking if any more dishes need to be cooked.
            jobState = StaffState.IDLE;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Finds any dishes that are below stock levels and returns them
     * Synchronized so that it is thread safe, that is that multiple Staff won't get the same dish returned and both restock the same dish.
     * @return Dish : Dish to restock.
     */
    private synchronized Dish findDishToRestock() {
        //Iterate through every dish we need to stock.
        for (Dish dish : stockManager.getDishStockLevels().keySet()) {
            StockItem stock = stockManager.getStockItem(dish);
            //If there are items where the restock threshold exceeds the number of prepared dishes, we need to make more.
            try {
                if (stock.getRestockThreshold() >= stock.getStock() && stockManager.canMakeMinQuantity(dish) && !stock.beingRestocked() && stockManager.getRestockThreshold(dish) != 0) {
                    stock.setBeingRestocked(true);
                    //If there are sufficient ingredients to make the restock amount
                    //Return the dish (to the staff to be made).
                    return dish;
                }
            } catch (InvalidStockItemException e) {
                e.printStackTrace();
            }
        }

        //Now we make sure that there are not greater orders of a dish than the restock threshold is (eg: If restock threshold
        //is set at 2 and stock falls to 3, but we get an order of 5 dishes this won't get stuck).
        for (Order order : orders) {
            if (order.getOrderState() == Order.OrderState.PREPARING) {
                for (Map.Entry<Dish, Number> dishOrdered : order.getBasket().entrySet()) {
                    Dish dish = dishOrdered.getKey();
                    Integer quantityOrdered = dishOrdered.getValue().intValue();
                    //If the amount we ordered is greater than the restock threshold and also greater than the amount in stock.
                    //We make the restock amount, this may take multiple repeats of this if it's a really big order.
                    try {
                        if (quantityOrdered > stockManager.getRestockThreshold(dish) && quantityOrdered > stockManager.getStockLevel(dish) && stockManager.getRestockThreshold(dish) != 0) {
                            return dish;
                        }
                    } catch (InvalidStockItemException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Performs checks on all orders being prepared if there are enough its Dishes for it to be finished preparing.
     */
    private void checkOrderComplete() {
        //Goes through orders, if there are enough of a stocked dish then its state is updated, otherwise it is cooked.
        for (Order order : orders) {
            if (order.getOrderState() == Order.OrderState.PREPARING) {
                try {
                    HashMap<Dish, Number> orderContent = order.getBasket();
                    boolean orderReady = true;
                    //Looks at each dish in the order and if there are sufficient in stock.
                    for (Map.Entry<Dish, Number> orderNumbers : orderContent.entrySet()) {
                        if (stockManager.getStockLevel(orderNumbers.getKey()) < orderNumbers.getValue().intValue()) {
                            orderReady = false;
                        }
                    }

                    if (orderReady) {
                        //Update the order status
                        order.setOrderState(Order.OrderState.PREPARED);
                        //Subtract the dish stock for the dishes we sold
                        stockManager.orderComplete(orderContent);
                    }
                } catch (InvalidStockItemException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns the current job summary of the staff member as a string
     * @return : Job summary
     */
    @Override
    public String toString() {
        return jobSummary();
    }

    /**
     * Returns the name of the staff member
     * @return : Name of staff member
     */
    @Override
    public String getName() {
        return this.staffName;
    }

    /**
     * Gets the current job of the staff member as a StaffState Enum value
     * @return StaffState representing current staff member job
     */
    public StaffState getJobState() {
        return this.jobState;
    }

    /**
     * Gets the current job of the staff member as a String
     * @return : String job summary
     */
    private String jobSummary() {
        if (this.jobState == StaffState.COOKING) {
            return "Cooking: " + currentlyMaking;
        } else if (this.jobState == StaffState.IDLE) {
            return "Idle";
        } else {
            return "";
        }
    }

    /**
     * Cancels this thread in the circumstance where a new Configuration file is loaded by the server
     */
    public void cancelThread() {
        this.threadRunning = false;
    }
}
