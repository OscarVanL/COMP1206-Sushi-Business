package common;

public class Drone extends Model implements Runnable {

    private String currentJobSummary;
    private int flyingSpeed;

    public Drone(int flyingSpeed) {
        notifyUpdate("instantiation", null, this);
        this.setName("Drone");
        this.flyingSpeed = flyingSpeed;
    }

    @Override
    public String toString() {
        return getName() + ": " + currentJobSummary;
    }

    @Override
    public String getName() {
        return super.name;
    }

    @Override
    public void run() {
        this.currentJobSummary="Idle";
        Ingredient toRestock;
        while(true) {
            System.out.println("Drone restocking not yet implemented");
        }
    }
}
