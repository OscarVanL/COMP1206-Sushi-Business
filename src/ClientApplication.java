import client.ClientInterface;
import client.ClientWindow;
import common.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Oscar van Leusen
 */
public class ClientApplication implements ClientInterface {

    private ClientWindow clientWindow;
    CommsClient comms;

    public static void main(String args[]) {
        ClientInterface clientInterface = initialise();
        ClientApplication app = (ClientApplication) clientInterface;
        ClientWindow window = app.launchGUI(clientInterface);
    }

    private static ClientInterface initialise() {
        ClientApplication app = new ClientApplication();

        try {
            app.comms = new CommsClient(app, 5000);
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
        User newUser = new User(username, password, address, postcode);
        comms.sendMessage(new Message(MessageType.REGISTER, newUser));
        return newUser;
    }

    //TODO: login communications
    @Override
    public User login(String username, String password) {

        return null;
    }

    @Override
    public List<Postcode> getPostcodes() {
        boolean success = comms.sendMessage(new Message(MessageType.GET_POSTCODES));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.POSTCODES);
            return (List<Postcode>) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public List<Dish> getDishes() {
        boolean success = comms.sendMessage(new Message(MessageType.GET_DISHES));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.DISHES);
            return (List<Dish>) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public String getDishDescription(Dish dish) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_DISH_DESC, dish));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.DISH_DESC);
            return (String) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public Number getDishPrice(Dish dish) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_DISH_PRICE, dish));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.DISH_PRICE);
            return (long) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public Map<Dish, Number> getBasket(User user) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_BASKET));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.BASKET);
            return (Map<Dish, Number>) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public Number getBasketCost(User user) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_BASKET_COST));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.BASKET_COST);
            return (long) receivedMessage.getPayload();
        }
        return null;
    }

    //TODO: Think about a better structure than this. List<Object> is a risky structure for different types
    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
        List<Object> dishToAdd = new ArrayList<>();
        dishToAdd.add(user);
        dishToAdd.add(dish);
        dishToAdd.add(quantity);
        comms.sendMessage(new Message(MessageType.SEND_DISH, dishToAdd));
    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
        //I call the add method here, because my add/update functionality in Order.class is the same as it checks for
        //pre-existing matching dishes.
        addDishToBasket(user, dish, quantity);
    }

    @Override
    public Order checkoutBasket(User user) {
        boolean success = comms.sendMessage(MessageType.SEND_CHECKOUT);
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.ORDER);
            return (Order) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public void clearBasket(User user) {
        comms.sendMessage(MessageType.SEND_CANCEL);
    }

    @Override
    public List<Order> getOrders(User user) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_ORDERS, user));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.ORDERS);
            return (List<Order>) receivedMessage.getPayload();
        }
        return null;
    }

    //TODO: Implement Order status logic
    @Override
    public boolean isOrderComplete(Order order) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_STATUS, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.STATUS);
            return false;
        }
        return false;
    }

    //TODO: Implement Order status logic
    @Override
    public String getOrderStatus(Order order) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_STATUS, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.STATUS);
            return "";
        }
        return "";
    }

    @Override
    public Number getOrderCost(Order order) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_COST, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.COST);
            return (long) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public void cancelOrder(Order order) {
        comms.sendMessage(new Message(MessageType.SEND_CANCEL, order));
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {

    }

    @Override
    public void notifyUpdate() {

    }
}
