package comms;

import exceptions.InvalidMessageException;

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

    private volatile boolean running;
    private CommsServer commsServer;
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int clientUID;
    private Queue<Message> messages = new LinkedList<>();
    private boolean firstPayload = true;

    CommsClientHandler(Socket socket, CommsServer commsServer) throws IOException {
        this.socket = socket;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.commsServer = commsServer;
        this.running = true;
    }

    /**
     * Thread that establishes the connection with the client and performs the handshake.
     * Then continues to read any messages received from the Client and stores them in a Queue.
     */
    @Override
    public void run() {
        Object receivedPayload;
        while (running) {
            try {
                //If this is the first thing we've received, it's the client telling us its UID, so store this.
                if (firstPayload) {
                    System.out.println("Waiting for first message from client");
                    receivedPayload = in.readObject();
                    clientUID = (int) receivedPayload;
                    System.out.println("Conncted to Client UID: " + clientUID);

                    firstPayload = false;

                } else {
                    Message received;
                    try {
                        received = (Message) in.readObject();
                    } catch (SocketException e) {
                        System.out.println("Client " + this.getUID() + " has disconnected.");
                        out.close();
                        in.close();
                        socket.close();
                        running = false;
                        Thread.currentThread().interrupt();
                        return;
                    }

                    if (received != null) {
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

    /**
     * Sends a Message to the Client
     * @param message : Message to send
     * @return : Boolean, True if sent without errors, False if not.
     * @throws InvalidMessageException : Thrown if a null message is attempted to be sent.
     */
    public boolean sendMessage(Message message) throws InvalidMessageException {
        if (message == null) {
            throw new InvalidMessageException("Attempted to send message to client with null content");
        }
        try {
            out.writeObject(message);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns any new messages received from the Client
     * @return : Message object from the Client
     */
    public Message receiveMessage() {
        synchronized (messages) {
            if (!messages.isEmpty()) {
                commsServer.setMessageStatus(false);
                return messages.remove();
            }
        }
        return null;
    }

    /**
     * Returns whether the message receiving functionality is running.
     * @return : True if it is, False if it is not.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Cancels this communication thread if a new Configuration is loaded on the Server
     */
    public void cancelThread() {
        this.running = false;
    }
}
