package common;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class Supplier extends Model implements Serializable {

    private String supplierName;
    private Integer distance;

    public Supplier(String name, Number distance) {
        notifyUpdate("instantiation", null, this);
        this.supplierName = name;
        this.distance = distance.intValue();
    }

    /**
     * Returns the supplier name
     * @return : String representation of supplier name
     */
    @Override
    public String getName() {
        return supplierName;
    }

    /**
     * Sets the supplier name
     * @param name : New supplier name
     */
    @Override
    public void setName(String name) {
        notifyUpdate("name", this.name, name);
        this.supplierName = name;
    }

    /**
     * Sets the distance that the supplier is from the Sushi restaurant.
     * @param distance : Distance from the restaurant.
     */
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
