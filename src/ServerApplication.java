import common.*;
import exceptions.*;
import server.ServerInterface;
import server.ServerWindow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Oscar van Leusen
 */
public class ServerApplication extends Thread implements ServerInterface {

    private ServerWindow serverWindow;
    private Configuration config;
    private static CommsServer communication;

    private StockManager stockManager = new StockManager();

    private ArrayList<UpdateListener> listeners = new ArrayList<>();
    private ArrayList<Supplier> suppliers = new ArrayList<>();
    private ArrayList<Ingredient> ingredients = new ArrayList<>();
    private ArrayList<Dish> dishes = new ArrayList<>();
    private ArrayList<Postcode> postcodes = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Order> orders = new ArrayList<>();
    private ArrayList<Staff> staff = new ArrayList<>();
    private ArrayList<Drone> drones = new ArrayList<>();

    public static boolean ingredientsRestocked = true;
    public static boolean dishesRestocked = true;

    public static void main(String args[]) {
        ServerInterface serverInterface = initialise();
        ServerApplication app = (ServerApplication) serverInterface;
        app.serverWindow = app.launchGUI(serverInterface);

        try {
            app.loadConfiguration("ConfigurationExample.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Thread commsThread;
        try {
            commsThread = new CommsServer(serverInterface, 5000);
            communication = (CommsServer) commsThread;
            commsThread.start();
            app.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            System.out.print("");
            if (communication.getMessageStatus()) {
                Message message = communication.receiveMessage();
                communication.setMessageStatus(false);
                System.out.println("Message received in serverApplication run()");
                if (message != null) {
                    processMessage(message);
                } else {
                    System.out.println("But message contents was null");
                }
            }
        }
    }

    private static ServerInterface initialise() {
        ServerApplication app = new ServerApplication();
        return app;
    }

    ServerWindow launchGUI(ServerInterface serverInterface) {
        ServerWindow window = new ServerWindow(serverInterface);
        this.serverWindow = window;
        return window;
    }

    /**
     * Tutorial for Java 8 file reading used: https://www.mkyong.com/java8/java-8-stream-read-a-file-line-by-line/
     * @param filename configuration file to load
     * @throws FileNotFoundException Exception thrown if the file is not found.
     */
    @Override
    public void loadConfiguration(String filename) throws FileNotFoundException {
        try {
            Server server = new Server(this, stockManager, users, orders);
            config = new Configuration(server, filename);
            config.loadConfiguration();
            notifyUpdate();
        } catch (InvalidSupplierException | InvalidStockItemException | InvalidIngredientException | InvalidPostcodeException | InvalidUserException | InvalidDishException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        this.ingredientsRestocked = enabled;
    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        this.dishesRestocked = enabled;

    }

    @Override
    public void setStock(Dish dish, Number stock) {
        try {
            stockManager.setStockLevel(dish, stock);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        try {
            stockManager.setStockLevel(ingredient, stock);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
    }

    @Override
    public List<Dish> getDishes() {
        return dishes;
    }

    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        Dish newDish = new Dish(name, description, price);
        try {
            StockItem newDishStock = new StockItem(newDish, 0, restockThreshold, restockAmount);
            stockManager.addDish(newDish, newDishStock);
            dishes.add(newDish);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
        return newDish;
    }

    @Override
    public void removeDish(Dish dish) throws UnableToDeleteException {
        //Checks to see if the Dish is contained in any Orders
        for (Order order : orders) {
            if (order.containsDish(dish)) {
                throw new UnableToDeleteException("Attempted to remove Dish that is contained in an order");
            }
        }
        //No exception was thrown, so dish cannot have been contained in any orders.
        dishes.remove(dish);
        stockManager.removeDish(dish);
        notifyUpdate();
    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        dish.addIngredient(ingredient, quantity);
        notifyUpdate();
    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        dish.removeIngredient(ingredient);
        notifyUpdate();
    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        dish.setRecipe(recipe);
        notifyUpdate();
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        try {
            stockManager.setRestockAmount(dish, restockAmount);
            stockManager.setRestockThreshold(dish, restockThreshold);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
    }

    @Override
    public Number getRestockThreshold(Dish dish) {
        try {
            return stockManager.getRestockThreshold(dish);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Number getRestockAmount(Dish dish) {
        try {
            return stockManager.getRestockAmount(dish);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return dish.getRecipe();
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return stockManager.getDishStockLevels();
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        Ingredient newIngredient  = new Ingredient(name, unit, supplier);
        StockItem newIngredientStock = null;
        try {
            newIngredientStock = new StockItem(newIngredient, 0, restockThreshold, restockAmount);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        stockManager.addIngredient(newIngredient, newIngredientStock);
        ingredients.add(newIngredient);
        notifyUpdate();
        return newIngredient;
    }

    @Override
    public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
        //Checks that the ingredient is not still used in any dish.
        boolean containedInDish = false;
        for (Dish dish : dishes) {
            if (dish.containsIngredient(ingredient)) {
                containedInDish = true;
            }
        }
        if (containedInDish) {
            throw new UnableToDeleteException("Attempted to remove Ingredient when it is still used in a Dish");
        } else {
            ingredients.remove(ingredient);
            stockManager.removeIngredient(ingredient);
        }
        notifyUpdate();
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        try {
            stockManager.setRestockThreshold(ingredient, restockThreshold);
            stockManager.setRestockAmount(ingredient, restockAmount);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
    }

    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        try {
            return stockManager.getRestockThreshold(ingredient);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        try {
            return stockManager.getRestockAmount(ingredient);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return stockManager.getIngredientStockLevels();
    }

    @Override
    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    @Override
    public Supplier addSupplier(String name, Number distance) {
        Supplier newSupplier = new Supplier(name, distance);
        suppliers.add(newSupplier);
        notifyUpdate();
        return newSupplier;
    }

    @Override
    public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
        //See if any ingredient uses this supplier, if it does, throw an UnableToDeleteException
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getSupplier().equals(supplier)) {
                throw new UnableToDeleteException("Attempted to remove Supplier that is still used for supplying ingredient: " + ingredient.getName());
            }
        }
        //If it got through those checks without any exception, we can remove it!
        suppliers.remove(supplier);
        notifyUpdate();
    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return supplier.getDistance();
    }

    @Override
    public List<Drone> getDrones() {
        return drones;
    }

    @Override
    public Drone addDrone(Number speed) {
        Drone newDrone = new Drone(speed);
        drones.add(newDrone);
        notifyUpdate();
        return newDrone;
    }

    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        drones.remove(drone);
        notifyUpdate();
    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return drone.getSpeed();
    }

    @Override
    public String getDroneStatus(Drone drone) {
        return drone.toString();
    }

    @Override
    public List<Staff> getStaff() {
        return staff;
    }

    @Override
    public Staff addStaff(String name) {
        Staff newStaff = new Staff(name, stockManager);
        staff.add(newStaff);
        notifyUpdate();
        return newStaff;
    }

    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        if (staff.getJobState() == Staff.JobState.IDLE) {
            this.staff.remove(staff);
            notifyUpdate();
        } else {
            throw new UnableToDeleteException("Attempted to remove Staff member when they are currently completing a job.");
        }

    }

    @Override
    public String getStaffStatus(Staff staff) {
        return staff.toString();
    }

    @Override
    public List<Order> getOrders() {
        return orders;
    }

    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {
        if (order.getOrderState() == Order.OrderState.CANCELLED) {
            orders.remove(order);
            notifyUpdate();
        } else {
            throw new UnableToDeleteException("Attempted to remove Order when it is not yet complete.");
        }

    }

    @Override
    public Number getOrderDistance(Order order) {
        return order.getUser().getPostcode().getDistance();
    }

    @Override
    public boolean isOrderComplete(Order order) {
        if (order.getOrderState() == Order.OrderState.COMPLETE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getOrderStatus(Order order) {
        Order.OrderState state = order.getOrderState();
        if (state == Order.OrderState.BASKET) {
            return "In Basket";
        } else if (state == Order.OrderState.PREPARING) {
            return "Preparing";
        } else if (state == Order.OrderState.DELIVERING) {
            return "Delivering";
        } else if (state == Order.OrderState.COMPLETE){
            return "Completed";
        } else if (state == Order.OrderState.CANCELLED) {
            return "Cancelled";
        } else {
            return "";
        }
    }

    @Override
    public Number getOrderCost(Order order) {
        return order.getOrderPrice();
    }

    @Override
    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    @Override
    public void addPostcode(String code, Number distance) {
        postcodes.add(new Postcode(code, distance));
        notifyUpdate();
    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        if (postcode.isDeleteSafe()) {
            postcodes.remove(postcode);
        } else {
            throw new UnableToDeleteException("Attempted to delete Postcode while not safe to delete Postcode");
        }
        notifyUpdate();
    }

    @Override
    public List<User> getUsers() {
        return this.users;
    }

    public User addUser(String username, String password, String location, Postcode postcode) {
        User newUser = new User(username, password, location, postcode);
        users.add(newUser);
        notifyUpdate();
        return newUser;
    }

    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        if (user.isDeleteSafe()) {
            users.remove(user);
        } else {
            throw new UnableToDeleteException("Attempted to delete User while not safe to delete user.");
        }
        notifyUpdate();
    }

    private void processMessage(Message message) {
        MessageType type = message.getType();

        if (type == MessageType.REGISTER) {
            System.out.println("Message Type: REGISTER");
            processRegister(message);
        } else if (type == MessageType.LOGIN) {
            System.out.println("Message Type: LOGIN");
            processLogin(message);
        } else if (type == MessageType.GET_POSTCODES) {
            System.out.println("Message Type: GET_POSTCODES");
            processGetPostcodes(message);
        } else if (type == MessageType.GET_DISHES) {
            System.out.println("Message Type: GET_DISHES");
            processGetDishes(message);
        } else if (type == MessageType.GET_DISH_DESC) {
            System.out.println("Message Type: GET_DISH_DESC");
            processGetDishDesc(message);
        } else if (type == MessageType.GET_DISH_PRICE) {
            System.out.println("Message Type: GET_DISH_PRICE");
            processGetDishPrice(message);
        } else if (type == MessageType.GET_BASKET) {
            System.out.println("Message Type: GET_BASKET");
            processGetBasket(message);
        } else if (type == MessageType.GET_BASKET_COST) {
            System.out.println("Message Type: GET_BASKET_COST");
            processGetBasketCost(message);
        } else if (type == MessageType.GET_ORDERS) {
            System.out.println("Message Type: GET_ORDERS");
            processGetOrders(message);
        } else if (type == MessageType.GET_STATUS) {
            System.out.println("Message Type: GET_STATUS");
            processGetOrderStatus(message);
        } else if (type == MessageType.GET_COST) {
            System.out.println("Message Type: GET_COST");
            processGetOrderCost(message);
        } else if (type == MessageType.ADD_DISH) {
            System.out.println("Message Type: SEND_DISH");
            processAddDishToBasket(message);
        } else if (type == MessageType.UPDATE_DISH) {
            System.out.println("Message Type: UPDATE_DISH");
            processUpdateDishInBasket(message);
        } else if (type == MessageType.SEND_CHECKOUT) {
            System.out.println("Message Type: SEND_CHECKOUT");
            processUserCheckout(message);
        } else if (type == MessageType.SEND_CLEAR) {
            System.out.println("Message Type: SEND_CLEAR");
            processBasketClear(message);
        } else if (type == MessageType.SEND_CANCEL) {
            System.out.println("Message Type: SEND_CANCEL");
            processOrderCancel(message);
        }
    }

    /**
     * Processes registration of new user from Client.
     * In our reply we return true/false as payload depending if registration was successful (eg: username already in use).
     * @param message : Message from Client.
     */
    private void processRegister(Message message) {
        int uid = message.getConnectionUID();
        User newUser = (User) message.getPayload();

        boolean userExists = false;
        for (User user : users) {
            if (user.getUsername().equals(newUser.getUsername())) {
                userExists = true;
            }
        }

        Message reply;
        if (!userExists) {
            newUser.setClientUID(uid);
            users.add(newUser);
            reply = new Message(MessageType.REGISTER_SUCCESS, true);
        } else {
            reply = new Message(MessageType.REGISTER_SUCCESS, false);
        }

        communication.sendMessage(uid, reply);
        notifyUpdate();
    }

    /**
     * Processes logins from Client.
     * We return the User object if successful, and return false if not.
     * @param message : Message from Client.
     */
    private void processLogin(Message message) {
        int uid = message.getConnectionUID();
        Message reply;
        ArrayList<String> loginDetails = (ArrayList<String>) message.getPayload();
        boolean loginCorrect = false;
        User loggedIn = null;
        for (User user : users) {
            //If there's a user where both username and password match those entered, it was a correct login!
            if (user.getUsername().equals(loginDetails.get(0)) && user.passwordMatches(loginDetails.get(1))) {
                loggedIn = user;
                loginCorrect = true;
            }
        }
        if (loginCorrect) {
            reply = new Message(MessageType.LOGIN_SUCCESS, loggedIn);
        } else {
            reply = new Message(MessageType.LOGIN_SUCCESS, null);
        }
        communication.sendMessage(uid, reply);
        notifyUpdate();
    }

    /**
     * Sends list of Postcodes from the Server to the Client
     * @param message : Request message from Client
     */
    private void processGetPostcodes(Message message) {
        int uid = message.getConnectionUID();
        Message reply = new Message(MessageType.POSTCODES, (ArrayList<Postcode>) this.getPostcodes());
        communication.sendMessage(uid, reply);
    }

    /**
     * Sends list of Dishes from the Server to the Client
     * @param message : Request message from Client
     */
    public void processGetDishes(Message message) {
        int uid = message.getConnectionUID();
        Message reply = new Message(MessageType.DISHES, (ArrayList<Dish>) this.getDishes());
        communication.sendMessage(uid, reply);
    }

    /**
     * Sends the description for a Dish requested by Client
     * @param message : Request message from Client
     */
    public void processGetDishDesc(Message message) {
        int uid = message.getConnectionUID();
        //The client Dish is considered inconsistent with the Server dish (which may have changed)
        //So we have to find the server's instance of the Dish (Names match).
        Dish clientDish = (Dish) message.getPayload();
        Dish serverDish = null;

        for (Dish dish : dishes) {
            if (dish.getName().equals(clientDish.getName())) {
                serverDish = dish;
            }
        }

        Message reply;
        if (serverDish != null) {
            reply = new Message(MessageType.DISH_DESC, serverDish.getDishDescription());
        } else {
            reply = new Message(MessageType.DISH_DESC, null);
        }
        communication.sendMessage(uid, reply);
    }

    /**
     * Sends the price of a Dish requested by the Client
     * @param message : Request message from Client
     */
    public void processGetDishPrice(Message message) {
        int uid = message.getConnectionUID();
        //The client Dish is considered inconsistent with the Server dish (which may have changed)
        //So we have to find the server's instance of the Dish (Names match).
        Dish clientDish = (Dish) message.getPayload();
        Dish serverDish = null;

        for (Dish dish : dishes) {
            if (dish.getName().equals(clientDish.getName())) {
                serverDish = dish;
            }
        }

        Message reply;
        if (serverDish != null) {
            reply = new Message(MessageType.DISH_PRICE, serverDish.getPrice());
        } else {
            reply = new Message(MessageType.DISH_PRICE, null);
        }

        communication.sendMessage(uid, reply);
    }

    /**
     * Sends the Basket associated with a given User requested by the Client
     * @param message : Request message from client
     */
    public void processGetBasket(Message message) {
        int uid = message.getConnectionUID();
        User clientUser = (User) message.getPayload();
        User serverUser = null;

        for (User user: users) {
            if (user.getUsername().equals(clientUser.getUsername())) {
                serverUser = user;
            }
        }

        Message reply = new Message(MessageType.BASKET, null);
        for (Order order : orders) {
            //If any Order object that is still in basket and for the requested user, this is the correct Order.
            if (order.getUser().equals(serverUser)) {
                reply = new Message(MessageType.BASKET, order.getBasket());
            }
        }
        communication.sendMessage(uid, reply);
    }

    /**
     * Gets the basket cost associated with a given User requested by the Client
     * @param message : Request message from Client
     */
    public void processGetBasketCost(Message message) {
        int uid = message.getConnectionUID();
        User clientUser = (User) message.getPayload();
        User serverUser = null;

        for (User user: users) {
            if (user.getUsername().equals(clientUser.getUsername())) {
                serverUser = user;
            }
        }

        Message reply = new Message(MessageType.BASKET_COST, null);
        for (Order order : orders) {
            if (order.getUser().equals(serverUser)) {
                reply = new Message(MessageType.BASKET_COST, order.getOrderPrice());
            }
        }
        communication.sendMessage(uid, reply);
    }

    /**
     * Gets all Orders for a particular user (if user is null, for all users) and sends to the Client
     * @param message : Request message from Client
     */
    public void processGetOrders(Message message) {
        int uid = message.getConnectionUID();
        User clientUser = (User) message.getPayload();

        Message reply;

        if (clientUser == null) {
            reply = new Message(MessageType.ORDERS, orders);
        } else {
            ArrayList<Order> userOrders = new ArrayList<>();

            for (Order order : orders) {
                if (order.getUser().getUsername().equals(clientUser.getUsername())) {
                    userOrders.add(order);
                }
            }
            reply = new Message(MessageType.ORDERS, userOrders);
        }

        communication.sendMessage(uid, reply);
    }

    /**
     * Gets the Order Status of a Order requested by the Client
     * @param message : Request message from Client
     */
    public void processGetOrderStatus(Message message) {
        int uid = message.getConnectionUID();
        Order clientOrder = (Order) message.getPayload();
        Order serverOrder = null;

        for (Order order : orders) {
            if (order.getUser().getName().equals(clientOrder.getUser().getName())) {
                serverOrder = order;
            }
        }

        String status = getOrderStatus(serverOrder);
        Message reply = new Message(MessageType.STATUS, status);
        communication.sendMessage(uid, reply);
    }

    /**
     * Gets the Order Cost of a Order requested by the Client
     * @param message : Request message from Client
     */
    public void processGetOrderCost(Message message) {
        int uid = message.getConnectionUID();
        Order clientOrder = (Order) message.getPayload();
        Order serverOrder = null;

        for (Order order : orders) {
            if (order.getUser().getName().equals(clientOrder.getUser().getName())) {
                serverOrder = order;
            }
        }

        Message reply = new Message(MessageType.COST, serverOrder.getOrderPrice());
        communication.sendMessage(uid, reply);
    }

    /**
     * Adds a Dish to the Basket in the Quantity requested by the Client
     * @param message : Request message from Client
     */
    public void processAddDishToBasket(Message message) {
        //Structure: [0]User user, [1]Dish dish, [2] Number quantity
        ArrayList<Object> dishData = (ArrayList<Object>) message.getPayload();
        //Instances of User and Dish that the client has (may be outdated)
        User clientUser = (User) dishData.get(0);
        Dish clientDish = (Dish) dishData.get(1);

        //We find instances of User and Dish that the server has (up to date)
        User serverUser = null;
        Dish serverDish = null;

        for (User user : users) {
            if (user.getUsername().equals(clientUser.getUsername())) {
                serverUser = user;
                break;
            }
        }

        for (Dish dish : dishes) {
            if (dish.getName().equals(clientDish.getName())) {
                serverDish = dish;
                break;
            }
        }

        //Finally, looks for the order belonging to the User and updates its quantity.
        for (Order order : orders) {
            if (order.getUser().equals(serverUser)) {
                order.addDish(serverDish, (int) dishData.get(2));
            }
        }

        notifyUpdate();
    }

    /**
     * Updates the quantity of a Dish in the Basket in the Quantity requested by the Client
     * Note: If this dish is not already in the basket, it is added.
     * @param message : Request message from Client
     */
    public void processUpdateDishInBasket(Message message) {
        //Structure: [0]User user, [1]Dish dish, [2] Number newQuantity
        ArrayList<Object> dishData = (ArrayList<Object>) message.getPayload();
        //Instances of User and Dish that the client has (may be outdated).
        User clientUser = (User) dishData.get(0);
        Dish clientDish = (Dish) dishData.get(1);

        //We find instances of User and Dish that the server has (up to date)
        User serverUser = null;
        Dish serverDish = null;

        for (User user : users) {
            if (user.getUsername().equals(clientUser.getUsername())) {
                serverUser = user;
                break;
            }
        }

        for (Dish dish : dishes) {
            if (dish.getName().equals(clientDish.getName())) {
                serverDish = dish;
                break;
            }
        }

        for (Order order : orders) {
            if (order.getUser().equals(serverUser)) {
                order.updateDishQuantity(serverDish, (int) dishData.get(2));
            }
        }

        notifyUpdate();
    }

    /**
     * Checks out the Order requested by the Client
     * @param message : Request message from Client
     */
    public void processUserCheckout(Message message) {
        int uid = message.getConnectionUID();
        //The User object the client has is considered outdated, so we need to look for the 'up to date' one on the server.
        User clientUser = (User) message.getPayload();
        User serverUser = null;

        for (User user : users) {
            if (user.getUsername().equals(clientUser.getUsername())) {
                serverUser = user;
            }
        }

        Message reply = new Message(MessageType.ORDER, null);
        for (Order order : orders) {
            if (order.getUser().equals(serverUser)) {
                order.setOrderState(Order.OrderState.PREPARING);
                reply = new Message(MessageType.ORDER, order);
            }
        }
        communication.sendMessage(uid, reply);
        notifyUpdate();
    }

    /**
     * Clears the Basket requested by the Client
     * @param message : Request message from Client
     */
    public void processBasketClear(Message message) {
        User clientUser = (User) message.getPayload();
        User serverUser = null;

        for (User user : users) {
            if (user.getUsername().equals(clientUser.getUsername())) {
                serverUser = user;
            }
        }
        //Look for the order belonging to this User that is still in the basket
        for (Order order : orders) {
            if (order.getUser().equals(serverUser) && order.getOrderState() == Order.OrderState.BASKET) {
                order.clear();
                break;
            }
        }

        notifyUpdate();
    }

    /**
     * Process the client request to cancel a given Order.
     * @param message : Request message from Client
     */
    public void processOrderCancel(Message message) {
        Order clientOrder = (Order) message.getPayload();

        for (Order order : orders) {
            if (order.getUser().getName().equals(clientOrder.getUser().getName())) {
                order.cancelOrder();
                notifyUpdate();
            }
        }
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
