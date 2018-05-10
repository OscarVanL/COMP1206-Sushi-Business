package common;

import java.io.Serializable;

public class Postcode extends Model implements Serializable {
    String postcode;

    public Postcode(String postcode) {
        notifyUpdate("instantiation", null, this);
        super.name = postcode;
        this.postcode = postcode;
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
