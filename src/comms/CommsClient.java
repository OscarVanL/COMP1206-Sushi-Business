package comms;

import client.ClientInterface;
import client.ClientWindow;
import common.UpdateEvent;
import exceptions.InvalidMessageException;

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
    private final ClientInterface client;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Queue<Message> messages = new LinkedList<>();
    private boolean firstMessage = true;

    /**
     * Accepts a new Server Connection, obtains ObjectInputStream and ObjectOutputStream and sends the client UID (hashCode)
     * @param client
     * @param port
     */
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

    /**
     * Runs the thread for receiving new messages from the Server.
     */
    @Override
    public void run() {
        while (running) {
            Message received = null;
            try {
                try {
                    received = (Message) in.readObject();
                } catch (SocketException | EOFException e) {
                    System.out.println("Server has closed.");
                    out.close();
                    in.close();
                    socket.close();
                    running = false;
                    firstMessage = true;
                    //Crash the client since it has no server connection and won't recover.
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

    /**
     * Thread for checking for Messages from the Server telling the Client to update
     */
    private void startServerNotifyCheck() {
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


    /**
     * Sends a message to the Server
     * @param message : Message object contain the type and payload (if any)
     * @return : Boolean on whether writeObject could complete without an error.
     */
    @Override
    public synchronized boolean sendMessage(Message message) {
        if (initialised()) {
            try {
                out.writeObject(message);
                out.flush();
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

        return false;
    }

    /**
     * Sends a message to a specific client UID, but since we are a client already this is ignored and sent to the server
     * @param uid : Unique ID to send message to (Not relevant)
     * @param message : Message to be sent
     * @return
     */
    @Override
    public synchronized boolean sendMessage(int uid, Message message) {
        //We just call sendMessage and ignore the uid, since only one instance of the server exists, so uid is redundant.
        return sendMessage(message);
    }

    /**
     * First received payload from server
     * @return : Serializable payload (must be cast to retrieve object)
     */
    @Override
    public synchronized Message receiveMessage() {
        //Tries to receive the message a few times because of timing differences. Bit of a hacky solution but oh well.
        do {
            for (int i=0; i<5; i++) {
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
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
    public synchronized Message receiveMessage(MessageType type) {
        do {
            for (int i=0; i<5; i++) {
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
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (messages.isEmpty());

        return null;
    }

    /**
     * Gets whether a new message has arrived from the Server
     * @return True if a new message has arrived, False if one has not.
     */
    @Override
    public boolean getMessageStatus() {
        return this.newMessage;
    }

    /**
     * Returns whether the Comms client has fully started and performed the server handshake.
     * @return : True of started, False if not.
     */
    public boolean initialised() {
        return !firstMessage;
    }
}
