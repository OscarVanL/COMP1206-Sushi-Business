package common;

import java.io.*;
import java.net.Socket;
import java.util.Queue;

/**
 * Tutorial used for implementation of Thread-based socket communication (although many aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsClientHandler extends Thread {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private int clientUID;
    private Queue<Message> messages;

    public CommsClientHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        Object receivedPayload;
        boolean firstPayload = true;
        while (true) {
            try {
                receivedPayload = in.readObject();
                //If this is the first thing we've received, it's the client telling us its UID, so store this.
                if (firstPayload) {
                    clientUID = (Integer) receivedPayload;
                } else {
                    messages.add((Message) receivedPayload);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the UID of the client this thread is connected to.
     * @return int : Unique Identifier generated from hashCode of ClientInterface connected to.
     */
    public int getUID() {
        return this.clientUID;
    }

    public boolean sendMessage(Serializable message) {
        try {
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Message receiveMessage() {
        return messages.remove();
    }
}
