package common;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class Supplier extends Model implements Serializable {

    String supplierName;
    private Integer distance;

    public Supplier(String name, Number distance) {
        notifyUpdate("instantiation", null, this);
        this.supplierName = name;
        this.distance = distance.intValue();
    }

    @Override
    public String toString() {
        return supplierName;
    }

    @Override
    public String getName() {
        return supplierName;
    }

    @Override
    public void setName(String name) {
        notifyUpdate("name", this.name, name);
        this.supplierName = name;
    }

    public void setDistance(Integer distance) {
        notifyUpdate("distance", this.distance, distance);
        this.distance = distance;
    }

    /**
     * Returns the distance of the Supplier from the Sushi restaurant
     * @return : Distance as a float
     */
    public Integer getDistance() {
        return this.distance;
    }
}
