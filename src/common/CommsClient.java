package common;

import client.ClientInterface;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static common.MessageType.*;

/**
 * Tutorial used for implementation of Thread-based socket communication (although many aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsClient extends Thread implements Comms {

    private boolean newMessage = false;
    private ClientInterface client;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final int port;
    private Queue<Message> messages = new LinkedList<>();
    private boolean firstMessage = true;

    public CommsClient(ClientInterface client, int port) {
        this.client = client;
        this.port = port;

        try {
            //Gets the localhost IP address
            InetAddress localIP = InetAddress.getLocalHost();
            //Opens a socket on this IP
            socket = new Socket(localIP, port);
            if (socket.isConnected()) {
                System.out.println("Client connected to server!");
            }

            System.out.println("out");
            out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("in");
            in = new ObjectInputStream(socket.getInputStream());
            //Passes the hashCode of the ClientInterface to act as a UID so that the server knows what client it's talking to.
            if (firstMessage) {
                out.writeObject(client.hashCode());
                System.out.println("sent hashcode");
                out.flush();
                firstMessage = false;
            }

            this.start();

            client.notifyAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            //client.notifyAll();
            System.out.println("checking");
            Message received = null;
            try {
                received = (Message) in.readObject();
                synchronized (messages) {
                    if (received != null) {
                        messages.add(received);
                        this.newMessage = true;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public boolean sendMessage(Serializable message) {
        try {
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendMessage(int uid, Serializable message) {
        //We just call sendMessage and ignore the uid, since only one instance of the server exists, so uid is redundant.
        return sendMessage(message);
    }

    /**
     * First received payload from server
     * @return : Serializable payload (must be cast to retrieve object)
     */
    @Override
    public Message receiveMessage() {
        //Tries to receive the message a few times because of timing differences. Bit of a hacky solution but oh well.
        do {
            synchronized (messages) {
                if (!messages.isEmpty()) {
                    return messages.remove();
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (messages.isEmpty());
        return null;
    }

    /**
     * First received payload from server, of a certain type.
     * @param type : Type of payload
     * @return : Message received
     */
    @Override
    public Message receiveMessage(MessageType type) {
        do {
            synchronized (messages) {
                for (Message message : messages) {
                    if (message.getType() == type) {
                        messages.remove(message);
                        return message;
                    }
                }
            }
        } while (messages.isEmpty());

        return null;
    }

    @Override
    public boolean getMessageStatus() {
        return this.newMessage;
    }

    public boolean initialised() {
        return !firstMessage;
    }
}
