package common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Oscar van Leusen
 */
public class Staff extends Model implements Runnable {

    private String currentJobSummary;
    private StockManager stockManager;

    public Staff(String staffName, StockManager stockManager) {
        super.setName(staffName);
        this.stockManager = stockManager;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + currentJobSummary;
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
                currentJobSummary = "cooking " + toRestock.getName();
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
            currentJobSummary = "idle";
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Staff break interrupted");
            }
        }
    }
}
