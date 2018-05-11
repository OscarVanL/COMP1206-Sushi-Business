package common;

import java.io.Serializable;

public class User extends Model implements Serializable {
    private String username;
    private String password;
    private String address;
    private Postcode postcode;

    public User(String username, String password, String address, Postcode postcode) {
        notifyUpdate("instantiation", null, this);
        this.username = username;
        this.password = password;
        this.address = address;
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
        return this.address;
    }

    public void setAddress(String newAddress) {
        notifyUpdate("address", this.address, newAddress);
        this.address = newAddress;
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
