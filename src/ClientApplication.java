import client.ClientInterface;
import client.ClientWindow;
import common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * @author Oscar van Leusen
 */
public class ClientApplication implements ClientInterface {

    //public static boolean ready = false;
    private ClientWindow clientWindow;
    private static CommsClient comms;
    private List<UpdateListener> listeners = new ArrayList<>();

    public static void main(String args[]) {
        ClientInterface clientInterface = initialise();
        ClientApplication app = (ClientApplication) clientInterface;

        new Thread(() -> {
            synchronized (app) {
                System.out.println("launching comms");
                CommsClient clientComms = new CommsClient(app, 5000);
                comms = clientComms;
            }
        }).start();

        System.out.println("back in main");
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ClientWindow window = app.launchGUI(clientInterface);
    }

    private static ClientInterface initialise() {
        ClientApplication app = new ClientApplication();
        return app;
    }

    ClientWindow launchGUI(ClientInterface clientInterface) {
        System.out.println("entered launchGUI");
        synchronized (this) {
            if (comms != null) {
                while (!comms.initialised()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            ClientWindow window = new ClientWindow(clientInterface);
            this.clientWindow = window;
            return window;
        }
    }

    @Override
    public User register(String username, String password, String address, Postcode postcode) {
        User newUser = new User(username, password, address, postcode);
        boolean success = comms.sendMessage(new Message(MessageType.REGISTER, newUser));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.REGISTER_SUCCESS);
            //REGISTER_SUCCESS messages return true or false boolean
            if ((boolean) receivedMessage.getPayload()) {
                notifyUpdate();
                return newUser;
            } else {
                notifyUpdate();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public User login(String username, String password) {
        ArrayList<String> loginDetails = new ArrayList();
        loginDetails.add(username);
        loginDetails.add(password);
        boolean success = comms.sendMessage(new Message(MessageType.LOGIN, loginDetails));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.LOGIN_SUCCESS);
            //LOGIN_SUCCESS message returns User object corresponding to the login used (or null if incorrect login)
            if (receivedMessage == null) {
                return null;
            } else if (receivedMessage.getPayload() == null) {
                return null;
            } else {
                return (User) receivedMessage.getPayload();
            }
        } else {
            notifyUpdate();
            return null;
        }
    }

    @Override
    public List<Postcode> getPostcodes() {
        boolean success = comms.sendMessage(new Message(MessageType.GET_POSTCODES));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.POSTCODES);
            return (ArrayList<Postcode>) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public List<Dish> getDishes() {
        boolean success = comms.sendMessage(new Message(MessageType.GET_DISHES));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.DISHES);
            return (ArrayList<Dish>) receivedMessage.getPayload();
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
        System.out.println(user.getName());
        boolean success = comms.sendMessage(new Message(MessageType.GET_BASKET, user));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.BASKET);
            return (Map<Dish, Number>) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public Number getBasketCost(User user) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_BASKET_COST, user));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.BASKET_COST);
            return (long) receivedMessage.getPayload();
        }
        return null;
    }

    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
        ArrayList<Object> dishToAdd = new ArrayList<>();
        dishToAdd.add(user);
        dishToAdd.add(dish);
        dishToAdd.add(quantity);
        comms.sendMessage(new Message(MessageType.ADD_DISH, dishToAdd));
        notifyUpdate();
    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
        ArrayList<Object> dishToUpdate = new ArrayList<>();
        dishToUpdate.add(user);
        dishToUpdate.add(dish);
        dishToUpdate.add(quantity);
        comms.sendMessage(new Message(MessageType.UPDATE_DISH, dishToUpdate));
        notifyUpdate();
    }

    @Override
    public Order checkoutBasket(User user) {
        boolean success = comms.sendMessage(new Message(MessageType.SEND_CHECKOUT, user));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.ORDER);
            notifyUpdate();
            if (receivedMessage.getPayload() == null) {
                return null;
            } else {
                return (Order) receivedMessage.getPayload();
            }
        }
        return null;
    }

    @Override
    public void clearBasket(User user) {
        comms.sendMessage(new Message(MessageType.SEND_CANCEL));
        notifyUpdate();
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

    @Override
    public boolean isOrderComplete(Order order) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_STATUS, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.STATUS);
            if (order.getOrderState() == Order.OrderState.COMPLETE | order.getOrderState() == Order.OrderState.CANCELLED) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public String getOrderStatus(Order order) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_STATUS, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.STATUS);
            return (String) receivedMessage.getPayload();
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
        notifyUpdate();
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        for (UpdateListener listener : listeners) {
            listener.updated(new UpdateEvent());
        }
    }
}
