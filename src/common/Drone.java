package common;

import java.util.ArrayList;


/**
 * @author Oscar van Leusen
 */
public class Drone extends Model implements Runnable {
    public enum DroneState {
        IDLE, FETCHING, DELIVERING
    }
    private volatile boolean threadRunning = true;
    private String droneName;
    private DroneState jobState;
    private StockManager stockManager;
    private ArrayList<Order> orders;
    private int flyingSpeed;
    private String currentlyRestocking = "";

    public Drone(Number flyingSpeed, StockManager stockManager, ArrayList<Order> orders, int droneID) {
        notifyUpdate("instantiation", null, this);
        this.droneName = "Drone " + droneID;
        this.jobState = DroneState.IDLE;
        this.stockManager = stockManager;
        this.flyingSpeed = (int) flyingSpeed;
        this.orders = orders;
    }

    @Override
    public void run() {
        Ingredient toRestock;
        //Restocks ingredients and delivers orders.
        while(threadRunning) {
            //Finds any ingredients that need to be restocked (returns null if there are none).
            toRestock = stockManager.findIngredientToRestock();

            if (toRestock != null) {
                setDroneState(DroneState.FETCHING);
                notifyUpdate();
                currentlyRestocking = toRestock.getName();
                try {
                    stockManager.restockIngredient(toRestock, flyingSpeed);
                } catch (InterruptedException e) {
                    System.out.println("Drone restocking interrupted");
                    break;
                }
                currentlyRestocking = "";
                setDroneState(DroneState.IDLE);
            }


            //Finds any orders that need to be delivered
            for (Order order : orders) {
                if (order.getOrderState() == Order.OrderState.PREPARED) {
                    setDroneState(DroneState.DELIVERING);
                    order.setOrderState(Order.OrderState.DELIVERING);
                    notifyUpdate();
                    try {
                        order.deliverOrder(this.flyingSpeed);
                        setDroneState(DroneState.IDLE);
                        notifyUpdate();
                    } catch (InterruptedException e) {
                        System.out.println("Drone delivery interrupted");
                        break;
                    }
                }
            }
        }
    }

    public DroneState getJobState() {
        return this.jobState;
    }

    private void setDroneState(DroneState state) {
        notifyUpdate("drone state", this.jobState, state);
        this.jobState = state;
    }

    private String jobSummary() {
        if (this.jobState == DroneState.IDLE) {
            return "Idle";
        } else if (this.jobState == DroneState.FETCHING) {
            return "Fetching ingredient: " + currentlyRestocking;
        } else if (this.jobState == DroneState.DELIVERING) {
            return "Delivering order";
        } else {
            return "";
        }
    }

    public int getSpeed() {
        return this.flyingSpeed;
    }

    @Override
    public String toString() {
        return jobSummary();
    }

    @Override
    public String getName() {
        return this.droneName;
    }

    public void cancelThread() {
        this.threadRunning = false;
    }
}
