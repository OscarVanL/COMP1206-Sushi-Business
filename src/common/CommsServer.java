package common;

import server.ServerInterface;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Tutorial used for implementation of Thread-based socket communication (although many aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsServer extends Thread implements Comms {
    private boolean newMessage = false;
    private ServerSocket serverSocket;
    private ServerInterface server;
    private int port;
    private ArrayList<Thread> clientConnections;

    public CommsServer(ServerInterface server, int port) throws IOException {
        this.server = server;
        this.port = port;
        serverSocket = new ServerSocket(port);
        clientConnections = new ArrayList<>();

    }

    @Override
    public void run() {
        while (true) {
            Socket socket = null;
            System.out.println("waiting for another connection");

            try {
                //Socket object receives incoming client requests, this is blocked until a client is connected
                socket = serverSocket.accept();
                System.out.println("A client has connected");

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                System.out.println("Assigning thread to this client");

                //TODO: Bug is here, clientConnections.add is never ran because thread is never left.
                Thread thread = new CommsClientHandler(socket, in, out, this, server);
                clientConnections.add(thread);
                thread.start();

            } catch (Exception e) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
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
        for (Thread thread : clientConnections) {
            CommsClientHandler client = (CommsClientHandler) thread;
            boolean messageSent = client.sendMessage(message);
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
        for (Thread thread : clientConnections) {
            CommsClientHandler client = (CommsClientHandler) thread;
            if (client.getUID() == uid) {
                return client.sendMessage(message);
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
        for (Thread thread : clientConnections) {
            CommsClientHandler client = (CommsClientHandler) thread;
            message = client.receiveMessage();
            if (!message.equals(null)) {
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
        for (Thread thread : clientConnections) {
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
}
