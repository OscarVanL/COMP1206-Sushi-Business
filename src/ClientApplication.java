import client.ClientInterface;
import client.ClientWindow;
import common.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ClientApplication implements ClientInterface {

    private ClientWindow clientWindow;
    CommsClient communication;

    public static void main(String args[]) {
        ClientInterface clientInterface = initialise();
        ClientApplication app = (ClientApplication) clientInterface;
        ClientWindow window = app.launchGUI(clientInterface);
    }

    private static ClientInterface initialise() {
        ClientApplication app = new ClientApplication();

        try {
            app.communication = new CommsClient(app, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return app;
    }

    ClientWindow launchGUI(ClientInterface clientInterface) {
        ClientWindow window = new ClientWindow(clientInterface);
        this.clientWindow = window;
        return window;
    }

    @Override
    public User register(String username, String password, String address, Postcode postcode) {
        //return new User(username, password, address, postcode);
        return null;
    }

    @Override
    public User login(String username, String password) {
        return null;
    }

    @Override
    public List<Postcode> getPostcodes() {
        return null;
    }

    @Override
    public List<Dish> getDishes() {
        return null;
    }

    @Override
    public String getDishDescription(Dish dish) {
        return null;
    }

    @Override
    public Number getDishPrice(Dish dish) {
        return null;
    }

    @Override
    public Map<Dish, Number> getBasket(User user) {
        return null;
    }

    @Override
    public Number getBasketCost(User user) {
        return null;
    }

    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {

    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {

    }

    @Override
    public Order checkoutBasket(User user) {
        return null;
    }

    @Override
    public void clearBasket(User user) {

    }

    @Override
    public List<Order> getOrders(User user) {
        return null;
    }

    @Override
    public boolean isOrderComplete(Order order) {
        return false;
    }

    @Override
    public String getOrderStatus(Order order) {
        return null;
    }

    @Override
    public Number getOrderCost(Order order) {
        return null;
    }

    @Override
    public void cancelOrder(Order order) {

    }

    @Override
    public void addUpdateListener(UpdateListener listener) {

    }

    @Override
    public void notifyUpdate() {

    }
}
