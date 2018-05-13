package common;

/**
 * @author Oscar van Leusen
 */
public class Supplier extends Model {

    private float distance;

    public Supplier(String name, float distance) {
        notifyUpdate("instantiation", null, this);
        super.setName(name);
        this.distance = distance;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        notifyUpdate("name", this.name, name);
        super.setName(name);
    }

    public void setDistance(float distance) {
        notifyUpdate("distance", this.distance, distance);
        this.distance = distance;
    }

    /**
     * Returns the distance of the Supplier from the Sushi restaurant
     * @return : Distance as a float
     */
    public float getDistance() {
        return this.distance;
    }
}
