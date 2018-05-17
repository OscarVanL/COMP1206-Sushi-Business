package common;

import client.ClientInterface;
import exceptions.InvalidMessageException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Tutorial used for implementation of Thread-based socket communication (although many aspects are changed from this):
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * @author Oscar van Leusen
 */
public class CommsClient extends Thread implements Comms {

    private volatile boolean running = false;
    private boolean newMessage = false;
    private boolean newUpdateNotify = false;
    private ClientInterface client;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Queue<Message> messages = new LinkedList<>();
    private boolean firstMessage = true;

    public CommsClient(ClientInterface client, int port) {
        this.client = client;

        try {
            //Gets the localhost IP address
            InetAddress localIP = InetAddress.getLocalHost();
            //Opens a socket on this IP
            socket = new Socket(localIP, port);
            if (socket.isConnected()) {
                running = true;
                System.out.println("Client connected to server!");

                out = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("Socket output setup");

                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("Socket input setup");
                //Passes the hashCode of the ClientInterface to act as a UID so that the server knows what client it's talking to.
                if (firstMessage) {
                    out.writeObject(client.hashCode());
                    System.out.println("sent hashcode");
                    out.flush();
                    firstMessage = false;
                }

                startServerNotifyCheck();

                this.start();
                client.notifyAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            Message received = null;
            try {
                try {
                    received = (Message) in.readObject();
                } catch (SocketException e) {
                    System.out.println("Server has closed.");
                    socket.close();
                    in.close();
                    out.close();
                    running = false;
                    firstMessage = true;
                    //Crash the client since it has no server connection
                    System.exit(-1);
                    return;
                }
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

    private void startServerNotifyCheck() {
        //Thread that checks for messages from the server telling the client to update.
        new Thread(() -> {
            while (running) {
                System.out.print("");
                if (newUpdateNotify) {
                    client.notifyUpdate();
                    System.out.println("Updated client GUI as requested by server.");
                    newUpdateNotify = false;
                }
            }
        }).start();
    }


    @Override
    public boolean sendMessage(Serializable message) {
        if (initialised()) {
            if (message instanceof Message) {
                try {
                    out.writeObject(message);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Sent invalid message type that wasn't of type 'Message'");
                try {
                    throw new InvalidMessageException("Sent invalid message type that wasn't of type 'Message'");
                } catch (InvalidMessageException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
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
                if (!messages.isEmpty() && initialised()) {
                    Message message = messages.remove();
                    if (message.getType() == MessageType.UPDATE) {
                        messages.remove(message);
                        this.newUpdateNotify = true;
                    }
                    return messages.remove();
                }
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
                    if (message.getType() == MessageType.UPDATE) {
                        messages.remove(message);
                        this.newUpdateNotify = true;
                    } else if (message.getType() == type) {
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
