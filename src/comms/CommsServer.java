package comms;

import exceptions.InvalidMessageException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tutorial used for implementation of Thread-based socket communication (although many aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsServer extends Thread implements Comms {
    private boolean running = true;
    private boolean newMessage = false;
    private int port;
    private ServerSocket serverSocket;
    private HashMap<Thread, Socket> clientConnection = new HashMap<>();

    public CommsServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
    }

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

            Socket socket = null;
            System.out.println("Waiting for a connection");

            try {
                //Socket object receives incoming client requests, this is blocked until a client is connected
                socket = serverSocket.accept();
                System.out.println("A client has connected");

                System.out.println("Assigning thread to this client");
                Thread thread = new CommsClientHandler(socket, this);
                clientConnection.put(thread, socket);
                thread.start();

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Sends message to every single client connected to
     * @param message : Message to send
     * @return boolean : Error sent without exceptions/errors
     */
    @Override
    public boolean sendMessage(Serializable message) {
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
    public boolean sendMessage(int uid, Serializable message) {
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
                System.out.println("Received message in CommsServer");
                return message;
            } else {
                System.out.println("Received message contained null.");
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

    @Override
    public boolean getMessageStatus() {
        return newMessage;
    }

    public void setMessageStatus(boolean newMessage) {
        this.newMessage = newMessage;
    }

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
