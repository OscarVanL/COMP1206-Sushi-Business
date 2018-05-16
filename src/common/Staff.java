package common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Oscar van Leusen
 */
public class Staff extends Model implements Runnable {
    public enum JobState {
        COOKING, IDLE
    }

    private String staffName;
    private JobState jobState;
    private StockManager stockManager;

    public Staff(String staffName, StockManager stockManager) {
        this.staffName = staffName;
        this.stockManager = stockManager;
    }

    @Override
    public String toString() {
        return staffName + ": " + getJobSummary();
    }

    @Override
    public String getName() {
        return super.name;
    }

    @Override
    public void run() {
        Dish toRestock;
        while(true) {
            //Finds any dishes that need to be restocked (returns null if there are none)
            toRestock = stockManager.findDishToRestock();
            if (toRestock != null) {
                jobState = JobState.COOKING;
                //Waits between 20 and 60 seconds while the cook makes the dishes
                int randomNum = ThreadLocalRandom.current().nextInt(20, 61);
                try {
                    Thread.sleep(1000*randomNum);
                } catch (InterruptedException e) {
                    System.out.println("Staff cooking meal interrupted");
                }
                //Adds the restock amount to the stock for this dish.
                stockManager.restockDish(toRestock);
            }

            //Waits another 10 seconds before checking if any more dishes need to be cooked.
            jobState = JobState.IDLE;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Staff break interrupted");
            }
        }
    }

    public JobState getJobState() {
        return this.jobState;
    }

    public String getJobSummary() {
        if (this.jobState == JobState.COOKING) {
            return "Cooking";
        } else if (this.jobState == JobState.IDLE) {
            return "Idle";
        } else {
            return "";
        }
    }
}
