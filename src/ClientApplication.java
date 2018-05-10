import client.ClientInterface;
import client.ClientWindow;

public class ClientApplication {

    public static void main(String args[]) {
        ClientInterface clientInterface = initialise();
        launchGUI(clientInterface);

    }

    static ClientInterface initialise() {
        return null;
    }

    static void launchGUI(ClientInterface clientInterface) {
        ClientWindow window = new ClientWindow(clientInterface);
    }
}
