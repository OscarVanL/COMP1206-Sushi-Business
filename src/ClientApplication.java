import client.ClientInterface;
import client.ClientWindow;
import common.*;
import comms.CommsClient;
import comms.Message;
import comms.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * @author Oscar van Leusen
 */
public class ClientApplication implements ClientInterface {

    //public static boolean ready = false;
    private static ClientWindow clientWindow;
    private static CommsClient comms;
    private User connectedUser;
    private List<UpdateListener> listeners = new ArrayList<>();
    private HashMap<Dish, Number> basket = new HashMap<>();

    /**
     * Starts the Client Application
     * Note: Must be started after the Server application.
     * @param args
     */
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

        //Waits a brief period after starting the server to ensure that the Comms thread is initialised before launching GUI
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clientWindow = app.launchGUI(clientInterface);
    }

    /**
     * Instantiates this ClientApplication
     * @return : ClientInterface instance
     */
    private static ClientInterface initialise() {
        ClientApplication app = new ClientApplication();
        return app;
    }

    /**
     * Launches the Client GUI
     * @param clientInterface : Previously initialised ClientInterface
     * @return ClientWIndow instance.
     */
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

    /**
     * Registers a user with the Register tab of the Client Window
     * @param username username : Username given by user
     * @param password password : Password given by user
     * @param address address : Address given by yser
     * @param postcode : valid postcode given by user
     * @return
     */
    @Override
    public User register(String username, String password, String address, Postcode postcode) {
        User newUser = new User(username, password, address, postcode);
        boolean success = comms.sendMessage(new Message(MessageType.REGISTER, newUser));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.REGISTER_SUCCESS);
            //REGISTER_SUCCESS messages return true or false boolean
            if ((boolean) receivedMessage.getPayload()) {
                notifyUpdate();
                this.connectedUser = newUser;
                return newUser;
            } else {
                notifyUpdate();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Logs in a user when the Login tab of the Client is used
     * @param username username : Username given by the User
     * @param password password : Password given by the user
     * @return
     */
    @Override
    public User login(String username, String password) {
        ArrayList<String> loginDetails = new ArrayList();
        loginDetails.add(username);
        loginDetails.add(password);
        //Send the login details to the server
        boolean success = comms.sendMessage(new Message(MessageType.LOGIN, loginDetails));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.LOGIN_SUCCESS);
            //LOGIN_SUCCESS message returns User object corresponding to the login used (or null if incorrect login)
            if (receivedMessage == null) {
                return null;
            } else if (receivedMessage.getPayload() == null) {
                return null;
            } else {
                //If the contents of the message aren't null (failed to login), returns the User
                this.connectedUser = (User) receivedMessage.getPayload();
                return connectedUser;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the List of Postcodes from the Server
     * @return : List of Postcodes
     */
    @Override
    public List<Postcode> getPostcodes() {
        boolean success = comms.sendMessage(new Message(MessageType.GET_POSTCODES));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.POSTCODES);
            //A very janky fix, but this solved a major problem I was having where getPostcodes (the first server-client request)
            //was returning null as receivedMessage. The client now launches reliably. It seems to only affect the first message.
            while (receivedMessage == null) {
                receivedMessage = comms.receiveMessage(MessageType.POSTCODES);
            }
            return (ArrayList<Postcode>) receivedMessage.getPayload();
        }
        return null;
    }

    /**
     * Gets the List of Dishes from the server
     * @return : List of Dishes
     */
    @Override
    public List<Dish> getDishes() {
        boolean success = comms.sendMessage(new Message(MessageType.GET_DISHES));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.DISHES);
            while (receivedMessage == null) {
                receivedMessage = comms.receiveMessage(MessageType.DISHES);
            }
            return (List<Dish>) receivedMessage.getPayload();
        }
        return null;
    }

    /**
     * Gets the Dish Description
     * @param dish Dish to lookup
     * @return
     */
    @Override
    public String getDishDescription(Dish dish) {
        return dish.getDishDescription();
    }

    /**
     * Gets the Dish price
     * @param dish Dish to lookup
     * @return
     */
    @Override
    public Number getDishPrice(Dish dish) {
        return dish.dishPrice();
    }

    /**
     * Gets the User Basket (stored locally until the user Checks Out)
     * @param user user to lookup
     * @return : Map<Dish, Number> representing the user's Basket
     */
    @Override
    public Map<Dish, Number> getBasket(User user) {
        return this.basket;
    }

    /**
     * Calculates the price of the User's basket
     * @param user user to lookup basket : Not actually used since basket is client side.
     * @return : Number containing Double representation of price
     */
    @Override
    public Number getBasketCost(User user) {
        Double cost = 0.00;

        for (Map.Entry<Dish, Number> basketDish : basket.entrySet()) {
            Dish dish = basketDish.getKey();
            Integer number = basketDish.getValue().intValue();
            cost += dish.dishPrice() * number;
        }

        return cost;
    }

    /**
     * Adds a Dish to the Basket. If it's already in the basket, adds to the number in the Basket
     * @param user user of basket : User whose basket should be changed (not used as Basket is held locally)
     * @param dish dish to change : Dish that should be added
     * @param quantity quantity to set : Number to add
     */
    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
        if (basket.keySet().contains(dish)) {
            int oldQuantity = basket.get(dish).intValue();
            basket.put(dish, quantity.intValue() + oldQuantity);
        } else {
            this.basket.put(dish, quantity);
        }
    }

    /**
     * Updates the quantity of a Dish in the basket
     * @param user user of basket (Not used since Basket is held Locally)
     * @param dish dish to change
     * @param quantity quantity to set. 0 should remove.
     */
    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
        if (quantity.intValue() == 0) {
            basket.remove(dish);
        } else {
            this.basket.put(dish, quantity);
        }
    }

    /**
     * Checks out the user's Basket with the Server
     * @param user user of basket (Not used since Basket is held Locally)
     * @return : New Order object from Server
     */
    @Override
    public Order checkoutBasket(User user) {
        Order order = new Order(user, user.getOrdersMade());
        user.incrementOrdersMade();
        order.addDishes(basket);

        boolean success = comms.sendMessage(new Message(MessageType.SEND_CHECKOUT, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.ORDER);
            if (receivedMessage == null) {
                return null;
            } else {
                this.clearBasket(user);
                return (Order) receivedMessage.getPayload();
            }
        }
        return null;
    }

    /**
     * Clears the contents of the Basket
     * @param user user of basket (Not used since basket is held Locally)
     */
    @Override
    public void clearBasket(User user) {
        this.basket.clear();
        notifyUpdate();
    }

    /**
     * Gets the orders for the given User (must have been sent to Checkout)
     * @param user user to lookup
     * @return List of Orders associated with this user
     */
    @Override
    public List<Order> getOrders(User user) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_ORDERS, user));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.ORDERS);
            while (receivedMessage == null) {
                receivedMessage = comms.receiveMessage(MessageType.ORDERS);
            }
            return (List<Order>) receivedMessage.getPayload();
        }
        return new ArrayList<>();
    }

    /**
     * Finds whether an Order is Complete or Cancelled
     * @param order order to lookup
     * @return True if OrderState is COMPLETE or CANCELLED.
     */
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

    /**
     * Gets Status of an Order as a String
     * @param order order to lookup
     * @return String status of Order
     */
    @Override
    public String getOrderStatus(Order order) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_STATUS, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.STATUS);
            while (receivedMessage == null) {
                receivedMessage = comms.receiveMessage(MessageType.STATUS);
            }
            return (String) receivedMessage.getPayload();
        }
        return "";
    }

    /**
     * Gets the Double cost of an Order
     * @param order to lookup
     * @return Double cost of Order
     */
    @Override
    public Number getOrderCost(Order order) {
        boolean success = comms.sendMessage(new Message(MessageType.GET_COST, order));
        if (success) {
            Message receivedMessage = comms.receiveMessage(MessageType.COST);
            while (receivedMessage == null) {
                receivedMessage = comms.receiveMessage(MessageType.COST);
            }
            return (Double) receivedMessage.getPayload();
        }
        return null;
    }

    /**
     * Sends a cancel request for an Order to the Server
     * @param order to cancel
     */
    @Override
    public void cancelOrder(Order order) {
        comms.sendMessage(new Message(MessageType.SEND_CANCEL, order));
        notifyUpdate();
    }

    /**
     * Adds update listeners
     * (only actually used by the ServerWindow, but implementation leaves this open for future addition).
     * @param listener An update listener to be informed of all model changes.
     */
    @Override
    public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Updates all update listeners for the Client
     * (only actually used by the ServerWindow, but implementation leaves this open for future addition).
     */
    @Override
    public void notifyUpdate() {
        clientWindow.updated(new UpdateEvent());
        for (UpdateListener listener : listeners) {
            listener.updated(new UpdateEvent());
        }
    }
}
