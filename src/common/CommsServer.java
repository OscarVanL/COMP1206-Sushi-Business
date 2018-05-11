package common;

import server.ServerInterface;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CommsServer implements Comms {

    private ServerSocket serverSocket;
    private ServerInterface server;
    private int port;
    private List<Thread> clientConnections;

    public CommsServer(ServerInterface server, int port) throws IOException {
        this.server = server;
        this.port = port;
        serverSocket = new ServerSocket(port);
        clientConnections = new ArrayList<>();

        while (true) {
            Socket socket = null;

            try {
                //Socket object receives incoming client requests, this is blocked until a client is connected
                socket = serverSocket.accept();
                System.out.println("A client has connected");

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                System.out.println("Assigning thread to this client");

                Thread thread = new CommsClientHandler(socket, in, out);
                clientConnections.add(thread);

                thread.start();
            } catch (Exception e) {
                socket.close();
                System.out.println(e);
            }
        }

    }

    /**
     * Sends message to every single client open
     * @param message
     */
    @Override
    public void sendMessage(Serializable ... message) {
        for (Thread thread : clientConnections) {
            CommsClientHandler client = (CommsClientHandler) thread;
            client.sendMessage(message);
        }
    }

    /**
     * Sends message to a specific client UID (HashCode)
     * @param uid : Client UID
     * @param message
     */
    @Override
    public void sendMessage(int uid, Serializable ... message) {
        for (Thread thread : clientConnections) {
            CommsClientHandler client = (CommsClientHandler) thread;
            if (client.getUID() == uid) {
                client.sendMessage(message);
            }
        }

    }

    /**
     * Receives a Message from any of the open clients
     * @return Message : Message read
     */
    @Override
    public Message receiveMessage() {
        Message message;
        for (Thread thread : clientConnections) {
            CommsClientHandler client = (CommsClientHandler) thread;
            message = client.receiveMessage();
            if (!message.equals(null)) {
                return message;
            }
        }
        return null;
    }
}
