package common;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class User extends Model implements Serializable {
    private String username;
    private String password;
    private String location;
    private Postcode postcode;

    public User(String username, String password, String location, Postcode postcode) {
        notifyUpdate("instantiation", null, this);
        this.username = username;
        this.password = password;
        this.location = location;
        this.postcode = postcode;
        super.setName(username);
    }

    public String getUsername() {
        return this.username;
    }

    public boolean passwordMatches(String password) {
        if (this.password.equals(password)) {
            return true;
        } else {
            return false;
        }
    }

    public String getAddress() {
        return this.location;
    }

    public void setAddress(String newAddress) {
        notifyUpdate("address", this.location, newAddress);
        this.location = newAddress;
    }

    public Postcode getPostcode() {
        return this.postcode;
    }

    public void setPostcode(Postcode newPostcode) {
        notifyUpdate("postcode", this.postcode, newPostcode);
        this.postcode = newPostcode;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
