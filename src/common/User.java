package common;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class User extends Model implements Serializable {
    private boolean deleteSafe = true;
    private String username;
    private String password;
    private String location;
    private Postcode postcode;
    public int ordersMade = 0;
    //The UID of the client this user is logged in to. (-1 if not logged in)
    private int clientUID = -1;

    public User(String username, String password, String location, Postcode postcode) {
        notifyUpdate("instantiation", null, this);
        this.username = username;
        this.password = password;
        this.location = location;
        this.postcode = postcode;
    }

    public boolean passwordMatches(String password) {
        if (this.password.equals(password)) {
            this.deleteSafe = false;
            return true;
        } else {
            return false;
        }
    }

    public void logout() {
        clientUID = -1;
        deleteSafe = true;
    }

    public boolean isLoggedIn() {
        if (clientUID != -1) {
            return true;
        } else {
            return false;
        }
    }

    public void setClientUID(int uid) {
        this.clientUID = uid;
    }

    public int getClientUID() {
        return this.clientUID;
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

    public boolean isDeleteSafe() {
        return deleteSafe;
    }

    @Override
    public String getName() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void incrementOrdersMade() {
        ordersMade++;
    }

    public int getOrdersMade() {
        return ordersMade;
    }
}
