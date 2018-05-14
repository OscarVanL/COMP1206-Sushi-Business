package common;

/**
 * @author Oscar van Leusen
 */
public class Supplier extends Model {

    private long distance;

    public Supplier(String name, Number distance) {
        notifyUpdate("instantiation", null, this);
        super.setName(name);
        this.distance = Long.valueOf(distance.longValue());
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

    public void setDistance(long distance) {
        notifyUpdate("distance", this.distance, distance);
        this.distance = distance;
    }

    /**
     * Returns the distance of the Supplier from the Sushi restaurant
     * @return : Distance as a float
     */
    public long getDistance() {
        return this.distance;
    }
}
