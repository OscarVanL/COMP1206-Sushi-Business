package common;

import server.ServerInterface;
import server.ServerWindow;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Tutorial used for implementation of Thread-based socket communication (although many aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsClientHandler extends Thread {

    private CommsServer commsServer;
    private ServerInterface serverApp;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private int clientUID;
    private Queue<Message> messages = new LinkedList<>();
    private boolean firstPayload = true;

    public CommsClientHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, CommsServer commsServer, ServerInterface serverApp) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.commsServer = commsServer;
        this.serverApp = serverApp;
    }

    @Override
    public void run() {
        Object receivedPayload;
        while (true) {
            System.out.println("entered while true");
            synchronized(serverApp) {
                try {
                    //If this is the first thing we've received, it's the client telling us its UID, so store this.
                    if (firstPayload) {

                        System.out.println("reading from client");
                        receivedPayload = in.readObject();
                        clientUID = (int) receivedPayload;
                        System.out.println("received client UID: " + clientUID);

                        out.flush();
                        firstPayload = false;

                    } else {
                        System.out.println("waiting for message");
                        Message received = null;
                        try {
                            received = (Message) in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (received != null) {
                            System.out.println("Received message!");
                            received.setConnectionUID(clientUID);
                            synchronized (messages) {
                                messages.add(received);
                                commsServer.setMessageStatus(true);
                                serverApp.notify();
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
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
        synchronized (messages) {
            commsServer.setMessageStatus(false);
            return messages.remove();
        }
    }
}
