package common;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class Postcode extends Model implements Serializable {
    private boolean deleteSafe = true;
    private String postcode;
    private Long distance;

    public Postcode(String postcode, Number distance) {
        notifyUpdate("instantiation", null, this);
        super.name = postcode;
        this.postcode = postcode;
        this.distance = distance.longValue();
    }

    /**
     * Gets the distance of this postcode from the Sushi restaurant
     * @return long : Distance from restaurant
     */
    public Long getDistance() {
        return this.distance;
    }

    public void setDeletable(boolean deletable) {
        this.deleteSafe = deletable;

    }

    public boolean isDeleteSafe() {
        return deleteSafe;
    }

    @Override
    public String toString() {
        return "Postcode: " + postcode;
    }


    @Override
    public String getName() {
        return this.postcode;
    }


}
