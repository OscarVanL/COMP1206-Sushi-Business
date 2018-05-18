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

    /**
     * Returns the current DroneState of the drone
     * @return : DroneState enum element representing drone state.
     */
    public DroneState getJobState() {
        return this.jobState;
    }

    /**
     * Sets the current DroneState of the drone
     * @param state : State to set
     */
    private void setDroneState(DroneState state) {
        notifyUpdate("drone state", this.jobState, state);
        this.jobState = state;
    }

    /**
     * Returns a string representation of the drone's current job
     * @return : String representing current job
     */
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

    /**
     * Gets the flying speed of the drone
     * @return : Integer speed of the drone
     */
    public int getSpeed() {
        return this.flyingSpeed;
    }

    /**
     * Gets the drone's job as a string
     * @return String : Drone's current job summary
     */
    @Override
    public String toString() {
        return jobSummary();
    }

    /**
     * Gets the drone's name ("Drone X").
     * @return String representation of drone name
     */
    @Override
    public String getName() {
        return this.droneName;
    }

    /**
     * Used to stop the drone when a new configuration is loaded.
     */
    public void cancelThread() {
        this.threadRunning = false;
    }
}
