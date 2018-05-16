package common;

import client.ClientInterface;

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

    private boolean running = false;
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


    @Override
    public boolean sendMessage(Serializable message) {
        if (initialised()) {
            try {
                //A tiny delay stops the client from sending requests faster than the server can process them.
                sleep(50);
                out.writeObject(message);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } else {
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
                if (!messages.isEmpty() && initialised()) {
                    return messages.remove();
                }
            }
            /**try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }**/
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
            /**try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }**/
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
