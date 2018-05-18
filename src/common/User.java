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
    private int ordersMade = 0;
    //The UID of the client this user is logged in to. (-1 if not logged in)
    private int clientUID = -1;

    public User(String username, String password, String location, Postcode postcode) {
        notifyUpdate("instantiation", null, this);
        this.username = username;
        this.password = password;
        this.location = location;
        this.postcode = postcode;
    }

    /**
     * Checks if the entered password matches the one on record
     * @param password : Entered password
     * @return True if passwords match, False if not.
     */
    public boolean passwordMatches(String password) {
        if (this.password.equals(password)) {
            this.deleteSafe = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Logs the user out by setting its connected client ID to -1 and makes it delete safe.
     */
    public void logout() {
        clientUID = -1;
        deleteSafe = true;
    }

    /**
     * Checks whether a User is logged on as a Client
     * @return : True if logged in, False if not.
     */
    public boolean isLoggedIn() {
        return clientUID != -1;
    }

    /**
     * Sets the client UID
     * @param uid : UID to set to
     */
    public void setClientUID(int uid) {
        this.clientUID = uid;
    }

    /**
     * Gets the client UID
     * @return : Client UID
     */
    public int getClientUID() {
        return this.clientUID;
    }

    /**
     * Gets the address of the User
     * @return : String address of User
     */
    public String getAddress() {
        return this.location;
    }

    /**
     * Sets the address of the user
     * @param newAddress : New address of the User
     */
    public void setAddress(String newAddress) {
        notifyUpdate("address", this.location, newAddress);
        this.location = newAddress;
    }

    /**
     * Gets the user's Postcode
     * @return Postcode for the User
     */
    public Postcode getPostcode() {
        return this.postcode;
    }

    /**
     * Sets the user's postcode
     * @param newPostcode : User's new postcode
     */
    public void setPostcode(Postcode newPostcode) {
        notifyUpdate("postcode", this.postcode, newPostcode);
        this.postcode = newPostcode;
    }

    /**
     * Checks whether the User is safe to delete (not logged into any clients)
     * @return : True if safe, False if not.
     */
    public boolean isDeleteSafe() {
        return deleteSafe;
    }

    /**
     * Returns the name of the User
     * @return : User name
     */
    @Override
    public String getName() {
        return this.username;
    }

    /**
     * Returns the password of the User
     * @return User password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Increments the number of orders the user has made for later order identification.
     */
    public void incrementOrdersMade() {
        ordersMade++;
    }

    /**
     * Gets the number of orders the User has made.
     * @return Number of orders the User has made.
     */
    public int getOrdersMade() {
        return ordersMade;
    }
}
