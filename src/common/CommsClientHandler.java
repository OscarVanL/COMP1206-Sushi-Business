package common;

import exceptions.InvalidMessageException;
import server.ServerInterface;
import server.ServerWindow;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Tutorial used for implementation of Thread-based socket communication (although many aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsClientHandler extends Thread {

    private boolean running = true;
    private CommsServer commsServer;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private int clientUID;
    private Queue<Message> messages = new LinkedList<>();
    private boolean firstPayload = true;

    public CommsClientHandler(Socket socket, CommsServer commsServer) throws IOException {
        this.socket = socket;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.commsServer = commsServer;
    }

    @Override
    public void run() {
        Object receivedPayload;
        while (running) {
            System.out.println("entered while true");
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
                    } catch (SocketException e) {
                        System.out.println("Client has disconnected.");
                        socket.close();
                        in.close();
                        out.close();
                        running = false;
                        Thread.currentThread().interrupt();
                        return;
                    }

                    if (received != null) {
                        System.out.println("Received message!");
                        received.setConnectionUID(clientUID);
                        synchronized (messages) {
                            messages.add(received);
                            commsServer.setMessageStatus(true);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                running = false;
                Thread.currentThread().interrupt();
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

    public boolean sendMessage(Serializable message) throws InvalidMessageException {
        if (message == null) {
            throw new InvalidMessageException("Attempted to send message to client with null content");
        }
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
            if (!messages.isEmpty()) {
                commsServer.setMessageStatus(false);
                return messages.remove();
            }
        }
        return null;
    }

    public boolean isRunning() {
        return running;
    }
}
