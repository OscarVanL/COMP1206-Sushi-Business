package comms;

import exceptions.InvalidMessageException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Tutorial used for implementation of Thread-based socket communication (although most aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsServer extends Thread implements Comms {
    private boolean running = true;
    private boolean newMessage = false;
    private ServerSocket serverSocket;
    private HashMap<Thread, Socket> clientConnection = new HashMap<>();

    public CommsServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }


    /**
     * Starts the Thread that accepts any new client connections and and then gives that client connection itself a new thread.
     */
    @Override
    public void run() {
        while (running) {

            //Makes sure to clear clientConnections of any closed connections
            for (Thread thread : clientConnection.keySet()) {
                CommsClientHandler comms = (CommsClientHandler) thread;
                if (!comms.isRunning()) {
                    clientConnection.remove(thread);
                }
            }

            Socket socket;
            try {
                //Socket object receives incoming client requests, this is blocked until a client is connected
                socket = serverSocket.accept();
                System.out.println("A client has connected");

                //Creates new thread for this client connection so that this thread is able to continue accepting
                //new connections.
                Thread thread = new CommsClientHandler(socket, this);
                clientConnection.put(thread, socket);
                thread.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends message to every single client connected to
     * @param message : Message to send
     * @return boolean : Error sent without exceptions/errors
     */
    @Override
    public synchronized boolean sendMessage(Message message) {
        boolean success = true;
        for (Thread thread : clientConnection.keySet()) {
            CommsClientHandler client = (CommsClientHandler) thread;
            boolean messageSent = false;
            try {
                messageSent = client.sendMessage(message);
            } catch (InvalidMessageException e) {
                e.printStackTrace();
            }
            if (!messageSent) {
                success = false;
            }
        }
        return success;
    }

    /**
     * Sends message to a specific client UID (HashCode)
     * @param uid : Client UID
     * @param message : Message to send
     * @return boolean: Error sent without exceptions/errors.
     */
    @Override
    public synchronized boolean sendMessage(int uid, Message message) {
        for (Thread thread : clientConnection.keySet()) {
            CommsClientHandler client = (CommsClientHandler) thread;
            if (client.getUID() == uid) {
                try {
                    return client.sendMessage(message);
                } catch (InvalidMessageException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Receives a Message from any of the open clients
     * @return Message : Message read
     */
    @Override
    synchronized public Message receiveMessage() {
        this.newMessage = false;
        Message message;
        for (Thread thread : clientConnection.keySet()) {
            CommsClientHandler client = (CommsClientHandler) thread;
            message = client.receiveMessage();
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    /**
     * Receives a Message from any of the open clients matching a specific type.
     * @param type : Type of message to return
     * @return Message : Message returned.
     */
    @Override
    synchronized public Message receiveMessage(MessageType type) {
        this.newMessage = false;
        Message message;
        for (Thread thread : clientConnection.keySet()) {
            CommsClientHandler client = (CommsClientHandler) thread;
            message = client.receiveMessage();
            if (message.getType() == type) {
                System.out.println("Received message of type: " + type);
                return message;
            }
        }
        return null;
    }

    /**
     * Returns whether there is a new message.
     * @return true : There is a new message, False : There is no new message
     */
    @Override
    public boolean getMessageStatus() {
        return newMessage;
    }

    /**
     * Sets whether there is a new message (set to false once the message has been read)
     * @param newMessage : State of message status.
     */
    public void setMessageStatus(boolean newMessage) {
        this.newMessage = newMessage;
    }

    /**
     * Used to drop all connections with clients when loading a new config file.
     * These clients will automatically close when the connection is lost.
     */
    public void dropConnections() {
        this.running = false;
        try {
            for (Map.Entry<Thread, Socket> client : clientConnection.entrySet()) {
                CommsClientHandler clientConnection = (CommsClientHandler) client.getKey();
                clientConnection.cancelThread();
                client.getValue().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.running = true;
    }
}
