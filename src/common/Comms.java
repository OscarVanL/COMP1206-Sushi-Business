package common;

import java.io.Serializable;

public interface Comms {

    //A message to be sent to all clients
    //Elipse adds varargs (arbitrary number of arguments)
    void sendMessage(Serializable ... payload);

    //Payload(s) to be sent to a client of specific uid
    //Elipse adds varargs (arbitrary number of arguments)
    void sendMessage(int uid, Serializable... payload);

    Message receiveMessage();
}
