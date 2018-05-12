package common;

import java.io.Serializable;

public interface Comms {

    //A message to be sent to all clients
    //Elipse adds varargs (arbitrary number of arguments)
    boolean sendMessage(Serializable payload);

    /**
     * Payload(s) to be sent to a client of specific uid
     * @param uid : Unique ID to send message to
     * @param payload : Object to be sent
     * @return boolean : Message sent without exceptions/errors.
     */
    boolean sendMessage(int uid, Serializable payload);

    Message receiveMessage();

    Message receiveMessage(MessageType type);
}
