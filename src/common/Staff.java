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

    boolean threadRunning = true;
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

    @Override
    public void run() {
        Dish toRestock;
        while(threadRunning) {
            System.out.println(getName() + ": ");
            //Finds any dishes that need to be restocked (returns null if there are none)
            toRestock = stockManager.findDishToRestock();
            if (toRestock != null) {
                jobState = StaffState.COOKING;
                //Adds the restock amount to the stock for this dish.
                currentlyMaking = toRestock.getName();
                stockManager.restockDish(toRestock);
                currentlyMaking = "";
                jobState = StaffState.IDLE;
            }

            checkOrderComplete();

            //Waits another 10 seconds before checking if any more dishes need to be cooked.
            jobState = StaffState.IDLE;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Staff break interrupted");
            }
        }
    }

    private void checkOrderComplete() {
        //Goes through orders, if there are enough of a stocked dish then its state is updated, otherwise it is cooked.
        for (Order order : orders) {
            try {
                HashMap<Dish, Integer> orderContent = order.getBasket();
                boolean orderReady = true;
                //Looks at each dish in the order and if there are sufficient in stock.
                for (Map.Entry<Dish, Integer> orderNumbers : orderContent.entrySet()) {
                    if (stockManager.getStockLevel(orderNumbers.getKey()) < orderNumbers.getValue()) {
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

    @Override
    public String toString() {
        return jobSummary();
    }

    @Override
    public String getName() {
        return this.staffName;
    }

    public StaffState getJobState() {
        return this.jobState;
    }

    public String jobSummary() {
        if (this.jobState == StaffState.COOKING) {
            return "Cooking: " + currentlyMaking;
        } else if (this.jobState == StaffState.IDLE) {
            return "Idle";
        } else {
            return "";
        }
    }

    public void cancelThread() {
        this.threadRunning = false;
    }
}
