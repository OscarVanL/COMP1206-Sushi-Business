package comms;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class Message implements Serializable {

    private MessageType type;
    private Serializable payload;
    //If this message was sent from a client or is directed to a client, it must contain its UID so that we know 'who' to send it to.
    private int connectionUID = 0;

    public Message(MessageType type, Serializable payload) {
        this.type = type;
        this.payload = payload;
    }

    /**
     * Used specifically for 'requests' that don't require any payload, such as GET_DISHES from Client.
     * @param requestType
     */
    public Message(MessageType requestType) {
        this.type = requestType;
        this.payload = new String("No Object");
    }

    /**
     * Gets the type of message being transmitted
     * @return : MessageType of this Message
     */
    public MessageType getType() {
        return this.type;
    }

    /**
     * Gets the payload object, if one exists.
     * @return : Payload Object, which can later be cast.
     */
    public Serializable getPayload() {
        return this.payload;
    }

    /**
     * Gets the UID the message was for, or is directed to
     * @return UID of client that sent the message or the client it is intended for.
     */
    public int getConnectionUID() {
        return this.connectionUID;
    }

    public void setConnectionUID(int uid) {
        this.connectionUID = uid;
    }
}
