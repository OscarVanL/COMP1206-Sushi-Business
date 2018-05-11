package common;

import client.ClientInterface;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Queue;

public class CommsClient implements Comms {

    private ClientInterface client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final int port;
    private Queue<Message> messages;

    public CommsClient(ClientInterface client, int port) throws IOException {
        this.client = client;
        this.port = port;
        InetAddress localIP = InetAddress.getLocalHost();
        Socket socket = new Socket(localIP, port);

        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());

        //Passes the hashCode of the ClientInterface to act as a UID so that the server knows what client it's talking to.
        out.writeInt(client.hashCode());

        while (true) {
            Message received = null;
            try {
                received = (Message) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            messages.add(received);
        }
    }


    @Override
    public void sendMessage(Serializable ... message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(int uid, Serializable ... message) {
        //We just call sendMessage and ignore the uid, since only one instance of the server exists, so uid is redundant.
        sendMessage(message);
    }

    /**
     * First received payload from server
     * @return : Serializable payload (must be cast to retrieve object)
     */
    @Override
    public Message receiveMessage() {
        return messages.remove();
    }
}
