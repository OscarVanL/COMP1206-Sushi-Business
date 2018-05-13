package common;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class Postcode extends Model implements Serializable {
    String postcode;
    long distance;

    public Postcode(String postcode, long distance) {
        notifyUpdate("instantiation", null, this);
        super.name = postcode;
        this.postcode = postcode;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Postcode: " + postcode;
    }

    @Override
    public String getName() {
        return this.name;
    }


}
