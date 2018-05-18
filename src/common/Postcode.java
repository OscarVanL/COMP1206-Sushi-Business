package common;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class Postcode extends Model implements Serializable {
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

    /**
     * Gets the postcode as String
     * @return : String containing the postcode represented by this object
     */
    @Override
    public String getName() {
        return this.postcode;
    }

}
