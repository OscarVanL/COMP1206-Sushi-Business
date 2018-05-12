package common;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private Object payload;

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    /**
     * Used specifically for 'requests' that don't require any payload, such as GET_DISHES from Client.
     * @param requestType
     */
    public Message(MessageType requestType) {
        this.type = requestType;
        this.payload = null;
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
    public Object getPayload() {
        return this.payload;
    }
}
