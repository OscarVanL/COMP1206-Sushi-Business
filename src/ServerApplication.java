import common.*;
import comms.CommsServer;
import comms.Message;
import comms.MessageType;
import exceptions.*;
import server.ServerInterface;
import server.ServerWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oscar van Leusen
 */
public class ServerApplication extends Thread implements ServerInterface {

    private static volatile boolean running = true;
    private static String configFile = "ConfigurationExample.txt";
    private static int portNumber;
    private Configuration config;
    private DataPersistence backup;
    private static CommsServer communication;

    private StockManager stockManager = new StockManager();

    private ArrayList<UpdateListener> listeners = new ArrayList<>();
    private ArrayList<Supplier> suppliers = new ArrayList<>();
    private ArrayList<Ingredient> ingredients = new ArrayList<>();
    private ArrayList<Dish> dishes = new ArrayList<>();
    private ArrayList<Postcode> postcodes = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Order> orders = new ArrayList<>();
    private HashMap<Staff, Thread> staff = new HashMap<>();
    private HashMap<Drone, Thread> drones = new HashMap<>();

    public static boolean ingredientsRestocked = true;
    public static boolean dishesRestocked = true;

    public static void main(String args[]) {
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            portNumber = Integer.parseInt(args[0]);
            configFile = args[1];
        }
        ServerInterface serverInterface = initialise();
        ServerApplication app = (ServerApplication) serverInterface;
        app.launchGUI(serverInterface);
    }

    /**
     * Used to constantly receive messages from Clients and process these for the Server response.
     */
    @Override
    public void run() {
        while (running) {
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

    /**
     * Initialises the Server Application and loads the default Configuration Example
     * @return ServerInterface used for launching GUI later
     */
    private static ServerInterface initialise() {
        ServerApplication app = new ServerApplication();
        app.loadConfiguration(configFile);
        return app;
    }

    /**
     * Launches the Server GUI
     * @param serverInterface : ServerInterface to launch GUI from
     * @return : ServerWindow
     */
    ServerWindow launchGUI(ServerInterface serverInterface) {
        return new ServerWindow(serverInterface);
    }

    /**
     * Starts the Comms Server and background message receiving thread.
     */
    private void startComms() {
        try {
            Thread commsThread = new CommsServer(portNumber);
            communication = (CommsServer) commsThread;
            commsThread.start();
            running = true;
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a configuration file, either with no initialised data or with already initialised data.
     * Tutorial for Java 8 file reading used: https://www.mkyong.com/java8/java-8-stream-read-a-file-line-by-line/
     * @param filename configuration file to load
     */
    @Override
    public void loadConfiguration(String filename) {
        //Before loading our configuration, close any existing threads and empty any stored values.
        for (Staff staffMember : staff.keySet()) {
            staffMember.cancelThread();
            staff.get(staffMember).interrupt();
        }
        for (Drone drone : drones.keySet()) {
            drone.cancelThread();
            drones.get(drone).interrupt();
        }
        stockManager = new StockManager();
        listeners.clear();
        suppliers.clear();
        ingredients.clear();
        dishes.clear();
        postcodes.clear();
        users.clear();
        orders.clear();
        staff.clear();
        drones.clear();
        ingredientsRestocked = true;
        dishesRestocked = true;
        if (communication != null) {
            communication.dropConnections();
        } else {
            startComms();
        }

        try {
            Server server = new Server(this, stockManager, users, orders);
            config = new Configuration(server, filename);
            config.loadConfiguration();
            backup = new DataPersistence(server, stockManager);
            Thread backupThread = new Thread(backup);
            backupThread.start();
            notifyUpdate();
        } catch (InvalidSupplierException | InvalidStockItemException | InvalidIngredientException | InvalidPostcodeException | InvalidUserException | InvalidDishException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Enables/disables restocking of Ingredients
     * @param enabled set to true to enable restocking of ingredients, or false to disable.
     */
    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        ingredientsRestocked = enabled;
    }

    /**
     * Enables/disables restocking of Dishes
     * @param enabled set to true to enable restocking of dishes, or false to disable.
     */
    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        dishesRestocked = enabled;

    }

    /**
     * Sets the stock levels of a specific Dish
     * @param dish dish to set the stock
     * @param stock stock amount
     */
    @Override
    public void setStock(Dish dish, Number stock) {
        try {
            stockManager.setStockLevel(dish, stock);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
    }

    /**
     * Sets the Stock of a specific Ingredient
     * @param ingredient ingredient to set the stock
     * @param stock stock amount
     */
    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        try {
            stockManager.setStockLevel(ingredient, stock);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
    }

    /**
     * Gets the List of Dishes stored on the Server
     * @return List of Dishes
     */
    @Override
    public List<Dish> getDishes() {
        return this.dishes;
    }

    /**
     * Adds a Dish to the Server
     * @param name name of dish
     * @param description description of dish
     * @param price price of dish
     * @param restockThreshold minimum threshold to reach before restocking
     * @param restockAmount amount to restock by
     * @return new Dish object
     */
    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        Dish newDish = new Dish(name, description, price, stockManager);
        System.out.println("New dish added: " + name);
        try {
            StockItem newDishStock = new StockItem(newDish, 0, restockThreshold, restockAmount);
            stockManager.addDish(newDish, newDishStock);
            dishes.add(newDish);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        notifyUpdate();
        notifyClient();
        return newDish;
    }

    /**
     * Removes a Dish from the Server
     * @param dish dish to remove
     * @throws UnableToDeleteException : If the Dish is in an order
     */
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
        notifyClient();
    }

    /**
     * Adds a new Ingredient to a Dish
     * @param dish dish to edit the recipe of
     * @param ingredient ingredient to add/update
     * @param quantity quantity to set. Should update and replace, not add to.
     */
    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        dish.addIngredient(ingredient, quantity);
        notifyUpdate();
    }

    /**
     * Removes an ingredient from a Dish
     * @param dish dish to edit the recipe of
     * @param ingredient ingredient to completely remove
     */
    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        dish.removeIngredient(ingredient);
        notifyUpdate();
    }

    /**
     * Sets the Recipe of a Dish
     * @param dish dish to modify the recipe of
     * @param recipe map of ingredients and quantity numbers to update
     */
    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        dish.setRecipe(recipe);
        notifyUpdate();
        //After tracing ServerWindow code, it is at this point the Dish details are finalised
        notifyClient();
    }

    /**
     * Sets the amount to restock by when restocking.
     * @param dish dish to modify the restocking levels of
     * @param restockThreshold new amount at which to restock
     * @param restockAmount new amount to restock by when threshold is reached
     */
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

    /**
     * Sets the threshold that stock must fall to before the Dish is to be restocked.
     * @param dish dish to query restock threshold of
     * @return Restock Threshold
     */
    @Override
    public Number getRestockThreshold(Dish dish) {
        try {
            return stockManager.getRestockThreshold(dish);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the amount that is restocked when a Dish's stock falls below the Retstock Threshold
     * @param dish dish to query restock amount of
     * @return Number to restock by
     */
    @Override
    public Number getRestockAmount(Dish dish) {
        try {
            return stockManager.getRestockAmount(dish);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the recipe of a Dish
     * @param dish dish to query the recipe of
     * @return Map of Ingredient to Number (quantity/amount)
     */
    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return dish.getRecipe();
    }

    /**
     * Gets the levels of stock of all Dishes
     * @return Map of Dish to Number in Stock
     */
    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return stockManager.getDishStockLevels();
    }

    /**
     * Gets a list of all Ingredients
     * @return List of all Ingredients
     */
    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Adds a new Ingredient
     * @param name name
     * @param unit unit
     * @param supplier supplier
     * @param restockThreshold when amount reaches restockThreshold restock
     * @param restockAmount when threshold is reached, restock with this amount
     * @return Ingredient added
     */
    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        Ingredient newIngredient  = new Ingredient(name, unit, supplier, stockManager);
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

    /**
     * Removes an Ingredient
     * @param ingredient ingredient to remove
     * @throws UnableToDeleteException Exception thrown if the Ingredient is contained in any dish
     */
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

    /**
     * Sets the restock levels for an Ingredient
     * @param ingredient ingredient to modify the restocking levels of
     * @param restockThreshold new amount at which to restock
     * @param restockAmount new amount to restock by when threshold is reached
     */
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

    /**
     * Gets the restock threshold for an Ingredient
     * @param ingredient ingredient to query restock threshold of
     * @return Restock Threshold value
     */
    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        try {
            return stockManager.getRestockThreshold(ingredient);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the Restock Amount for an Ingredient
     * @param ingredient ingredient to query restock amount of
     * @return Restock Amount value
     */
    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        try {
            return stockManager.getRestockAmount(ingredient);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the stock levels for all Ingredients
     * @return : Map of Ingredient to Stock number
     */
    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return stockManager.getIngredientStockLevels();
    }

    /**
     * Gets a List of all Suppliers
     * @return List of all Suppliers
     */
    @Override
    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    /**
     * Adds a new Supplier to the list of Suppliers
     * @param name name of supplier
     * @param distance from restaurant
     * @return : New supplier Object
     */
    @Override
    public Supplier addSupplier(String name, Number distance) {
        Supplier newSupplier = new Supplier(name, distance);
        suppliers.add(newSupplier);
        notifyUpdate();
        return newSupplier;
    }

    /**
     * Attempts to remove a Supplier
     * @param supplier supplier to remove
     * @throws UnableToDeleteException : Exception thrown if the Supplier is a supplier to any of the Ingredients in the Restaurant
     */
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

    /**
     * Gets the distance a Supplier is away from the restaurant
     * @param supplier supplier to query
     * @return Distance from Restaurant
     */
    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return supplier.getDistance();
    }

    /**
     * Gets a List of all the Drones
     * @return List of all the Drones
     */
    @Override
    public List<Drone> getDrones() {
        return new ArrayList<>(drones.keySet());
    }

    /**
     * Adds a new Drone to the restaurant
     * @param speed speed of drone
     * @return : Drone Object just added
     */
    @Override
    public Drone addDrone(Number speed) {
        Drone newDrone = new Drone(speed, stockManager, orders, drones.size() + 1);
        Thread droneThread = new Thread(newDrone);
        droneThread.start();
        drones.put(newDrone, droneThread);
        notifyUpdate();
        return newDrone;
    }

    /**
     * Removes a Drone from the restaurant
     * @param drone drone to remove
     * @throws UnableToDeleteException : Throws exception if the Drone is currently busy with a job.
     */
    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        if (drone.getJobState() == Drone.DroneState.IDLE) {
            drones.remove(drone);
            notifyUpdate();
        } else {
            throw new UnableToDeleteException("Attempted to Delete Drone while it is busy with a job.");
        }
    }

    /**
     * Gets the speed that a Drone can travel at
     * @param drone drone to query
     * @return Drone speed number
     */
    @Override
    public Number getDroneSpeed(Drone drone) {
        return drone.getSpeed();
    }

    /**
     * Gets the Status of the Drone as a String
     * @param drone drone to query
     * @return Drone Status
     */
    @Override
    public String getDroneStatus(Drone drone) {
        return drone.toString();
    }

    /**
     * Gets a list of Staff working for the restaurant
     * @return : List of Staff
     */
    @Override
    public List<Staff> getStaff() {
        return new ArrayList<>(staff.keySet());
    }

    /**
     * Adds a staff member to the restaurant
     * @param name name of staff member
     * @return Staff Member Object
     */
    @Override
    public Staff addStaff(String name) {
        Staff newStaff = new Staff(name, stockManager, orders);
        Thread staffThread = new Thread(newStaff);
        staffThread.start();
        staff.put(newStaff, staffThread);
        notifyUpdate();
        return newStaff;
    }

    /**
     * Removes the Staff Member from the restaurant
     * @param staff staff member to remove
     * @throws UnableToDeleteException : Thrown if the Staff Member is busy with a job.
     */
    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        if (staff.getJobState() == Staff.StaffState.IDLE) {
            this.staff.remove(staff);
            notifyUpdate();
        } else {
            throw new UnableToDeleteException("Attempted to remove Staff member when they are currently completing a job.");
        }

    }

    /**
     * Gets the current Job of the Staff Member
     * @param staff member to query
     * @return String representation of staff member's job
     */
    @Override
    public String getStaffStatus(Staff staff) {
        return staff.toString();
    }

    /**
     * Gets a list of Orders
     * @return List of Orders
     */
    @Override
    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Removes an order from the Application
     * @param order order to remove
     * @throws UnableToDeleteException Exception thrown if the order is not cancelled or complete yet.
     */
    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {
        if (order.getOrderState() == Order.OrderState.CANCELLED || order.getOrderState() == Order.OrderState.COMPLETE) {
            orders.remove(order);
            notifyUpdate();
        } else {
            throw new UnableToDeleteException("Attempted to remove Order when it is not yet complete.");
        }

    }

    /**
     * Gets the distance needed to be travelled by the Drone to deliver the order
     * @param order order to query
     * @return Distance needed to be travelled
     */
    @Override
    public Number getOrderDistance(Order order) {
        return order.getUser().getPostcode().getDistance();
    }

    /**
     * Return whether the Order is Complete
     * @param order order to query
     * @return True if Complete, False if not.
     */
    @Override
    public boolean isOrderComplete(Order order) {
        return order.getOrderState() == Order.OrderState.COMPLETE;
    }

    /**
     * Gets a String representation of the Order's status
     * @param order order to query
     * @return String order status
     */
    @Override
    public String getOrderStatus(Order order) {
        if (order == null) {
            return "In Basket";
        }
        Order.OrderState state = order.getOrderState();
        if (state == Order.OrderState.BASKET) {
            return "In Basket";
        } else if (state == Order.OrderState.PREPARING) {
            return "Preparing";
        } else if (state == Order.OrderState.PREPARED) {
            return "Prepared";
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

    /**
     * Gets then cost of the Order
     * @param order order to query
     * @return Cost of Order
     */
    @Override
    public Number getOrderCost(Order order) {
        return order.orderPrice();
    }

    /**
     * Gets the list of Postcodes
     * @return : List of postcodes
     */
    @Override
    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    /**
     * Adds a postcode to the deliverable postcodes for the restaurant
     * @param code postcode string representation
     * @param distance distance from the restaurant
     */
    @Override
    public void addPostcode(String code, Number distance) {
        postcodes.add(new Postcode(code, distance));
        notifyUpdate();
    }

    /**
     * Removes a postcode from the deliverable postcodes for the restaurant
     * @param postcode postcode to remove
     * @throws UnableToDeleteException : Exception thrown if the postcode is already used in a User's address details.
     */
    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        for (User user : users) {
            if (user.getPostcode().getName().equals(postcode.getName())) {
                throw new UnableToDeleteException("Attempted to delete Postcode while not safe to delete Postcode");
            }
        }
        postcodes.remove(postcode);
        notifyUpdate();
    }

    /**
     * Returns the List of Users registered
     * @return List of Users
     */
    @Override
    public List<User> getUsers() {
        return this.users;
    }

    /**
     * Registers/adds a User to the restaurant
     * @param username : Username of new user
     * @param password : Password of new user
     * @param location : Location of new User
     * @param postcode : Postcode of new User
     * @return Instance of new User
     */
    public User addUser(String username, String password, String location, Postcode postcode) {
        User newUser = new User(username, password, location, postcode);
        users.add(newUser);
        notifyUpdate();
        return newUser;
    }

    /**
     * Removes a user from the restaurant.
     * @param user to remove
     * @throws UnableToDeleteException : Exception thrown if user has open order
     */
    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        for (Order order : orders) {
            if (order.getUser().equals(user)) {
                throw new UnableToDeleteException("Attempted to delete User while not safe to delete user.");
            }
        }
        users.remove(user);
        notifyUpdate();
    }

    /**
     * Processes received Messages by finding their type and calling the relevant method to deal with it.
     * @param message Message received from Client.
     */
    private void processMessage(Message message) {
        MessageType type = message.getType();

        if (type == MessageType.REGISTER) {
            processRegister(message);
        } else if (type == MessageType.LOGIN) {
            processLogin(message);
        } else if (type == MessageType.GET_POSTCODES) {
            processGetPostcodes(message);
        } else if (type == MessageType.GET_DISHES) {
            processGetDishes(message);
        } else if (type == MessageType.GET_DISH_DESC) {
            processGetDishDesc(message);
        } else if (type == MessageType.GET_DISH_PRICE) {
            processGetDishPrice(message);
        } else if (type == MessageType.GET_BASKET) {
            processGetBasket(message);
        } else if (type == MessageType.GET_BASKET_COST) {
            processGetBasketCost(message);
        } else if (type == MessageType.GET_ORDERS) {
            processGetOrders(message);
        } else if (type == MessageType.GET_STATUS) {
            processGetOrderStatus(message);
        } else if (type == MessageType.GET_COST) {
            processGetOrderCost(message);
        } else if (type == MessageType.ADD_DISH) {
            processAddDishToBasket(message);
        } else if (type == MessageType.UPDATE_DISH) {
            processUpdateDishInBasket(message);
        } else if (type == MessageType.SEND_CHECKOUT) {
            processUserCheckout(message);
        } else if (type == MessageType.SEND_CLEAR) {
            processBasketClear(message);
        } else if (type == MessageType.SEND_CANCEL) {
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
            if (user.getName().equals(newUser.getName())) {
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
            if (user.getName().equals(loginDetails.get(0)) && user.passwordMatches(loginDetails.get(1))) {
                loggedIn = user;
                loginCorrect = true;
                break;
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
        Message reply;
        if (this.getPostcodes() == null) {
            System.out.println("Postcodes not yet initialised...");
            reply = new Message(MessageType.POSTCODES, new ArrayList<Postcode>());
        } else {
            reply = new Message(MessageType.POSTCODES, (ArrayList<Postcode>) this.getPostcodes());
        }

        communication.sendMessage(uid, reply);
    }

    /**
     * Sends list of Dishes from the Server to the Client
     * @param message : Request message from Client
     */
    private void processGetDishes(Message message) {
        int uid = message.getConnectionUID();
        Message reply = new Message(MessageType.DISHES, (ArrayList<Dish>) this.getDishes());
        communication.sendMessage(uid, reply);
    }

    /**
     * Sends the description for a Dish requested by Client
     * @param message : Request message from Client
     */
    private void processGetDishDesc(Message message) {
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
    private void processGetDishPrice(Message message) {
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
            reply = new Message(MessageType.DISH_PRICE, serverDish.dishPrice());
        } else {
            reply = new Message(MessageType.DISH_PRICE, null);
        }

        communication.sendMessage(uid, reply);
    }

    /**
     * Sends the Basket associated with a given User requested by the Client
     * @param message : Request message from client
     */
    private void processGetBasket(Message message) {
        int uid = message.getConnectionUID();

        Message reply = new Message(MessageType.BASKET, null);

        //If no User was passed with message, return an empty basket.
        User clientUser = (User) message.getPayload();
        User serverUser = null;

        for (User user: users) {
            if (user.getName().equals(clientUser.getName())) {
                serverUser = user;
            }
        }

        for (Order order : orders) {
            //If any Order object that is still in basket and for the requested user, this is the correct Order.
            if (order.getUser().equals(serverUser) && order.getOrderState() == Order.OrderState.BASKET) {
                reply = new Message(MessageType.BASKET, order.getBasket());
            }
        }

        communication.sendMessage(uid, reply);
    }

    /**
     * Gets the basket cost associated with a given User requested by the Client
     * @param message : Request message from Client
     */
    private void processGetBasketCost(Message message) {
        int uid = message.getConnectionUID();
        User clientUser = (User) message.getPayload();
        User serverUser = null;

        for (User user: users) {
            if (user.getName().equals(clientUser.getName())) {
                serverUser = user;
            }
        }

        Message reply = new Message(MessageType.BASKET_COST, null);
        for (Order order : orders) {
            if (order.getUser().equals(serverUser) && order.getOrderState() == Order.OrderState.BASKET) {
                reply = new Message(MessageType.BASKET_COST, order.orderPrice());
            }
        }
        communication.sendMessage(uid, reply);
    }

    /**
     * Gets all Orders for a particular user (if user is null, for all users) and sends to the Client
     * @param message : Request message from Client
     */
    private void processGetOrders(Message message) {
        int uid = message.getConnectionUID();
        User clientUser = (User) message.getPayload();

        Message reply = new Message(MessageType.ORDERS, orders);

        if (clientUser != null) {
            ArrayList<Order> userOrders = new ArrayList<>();

            for (Order order : orders) {
                if (order.getUser().getName().equals(clientUser.getName()) && order.getOrderState() != Order.OrderState.BASKET) {
                    userOrders.add(order);
                }
            }
            if (userOrders.size() > 0) {
                reply = new Message(MessageType.ORDERS, userOrders);
            }
        }

        communication.sendMessage(uid, reply);
    }

    /**
     * Gets the Order Status of a Order requested by the Client
     * @param message : Request message from Client
     */
    private void processGetOrderStatus(Message message) {
        int uid = message.getConnectionUID();
        Order clientOrder = (Order) message.getPayload();

        Message reply = new Message(MessageType.STATUS, "In Basket");

        for (Order order : orders) {
            if (order.getUser().getName().equals(clientOrder.getUser().getName()) && order.getUserOrderNum() == clientOrder.getUserOrderNum()) {
                reply = new Message(MessageType.STATUS, getOrderStatus(order));
            }
        }

        communication.sendMessage(uid, reply);
    }

    /**
     * Gets the Order Cost of a Order requested by the Client
     * @param message : Request message from Client
     */
    private void processGetOrderCost(Message message) {
        int uid = message.getConnectionUID();
        Order clientOrder = (Order) message.getPayload();
        Order serverOrder = null;

        for (Order order : orders) {
            if (order.getUser().getName().equals(clientOrder.getUser().getName()) && clientOrder.getUserOrderNum() == order.getUserOrderNum()) {
                serverOrder = order;
            }
        }

        Message reply = new Message(MessageType.COST, serverOrder.orderPrice());
        communication.sendMessage(uid, reply);
    }

    /**
     * Adds a Dish to the Basket in the Quantity requested by the Client
     * @param message : Request message from Client
     */
    private void processAddDishToBasket(Message message) {
        //Structure: [0]User user, [1]Dish dish, [2] Number quantity
        ArrayList<Object> dishData = (ArrayList<Object>) message.getPayload();
        //Instances of User and Dish that the client has (may be outdated)
        User clientUser = (User) dishData.get(0);
        Dish clientDish = (Dish) dishData.get(1);

        //We find instances of User and Dish that the server has (up to date)
        User serverUser = null;
        Dish serverDish = null;

        for (User user : users) {
            if (user.getName().equals(clientUser.getName())) {
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
    private void processUpdateDishInBasket(Message message) {
        //Structure: [0]User user, [1]Dish dish, [2] Number newQuantity
        ArrayList<Object> dishData = (ArrayList<Object>) message.getPayload();
        //Instances of User and Dish that the client has (may be outdated).
        User clientUser = (User) dishData.get(0);
        Dish clientDish = (Dish) dishData.get(1);

        //We find instances of User and Dish that the server has (up to date)
        User serverUser = null;
        Dish serverDish = null;

        for (User user : users) {
            if (user.getName().equals(clientUser.getName())) {
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
    private void processUserCheckout(Message message) {
        int uid = message.getConnectionUID();
        Order order = (Order) message.getPayload();
        Dish clientDish;
        Dish serverDish = null;
        HashMap<Dish, Number> serverOrderData = new HashMap<>();

        //Gets all items in the basket and replaces the Dish with the server-side instance of the dish.
        for (Map.Entry<Dish, Number> orderData : order.getBasket().entrySet()) {
            clientDish = orderData.getKey();
            for (Dish dish : dishes) {
                if (clientDish.getName().equals(dish.getName())) {
                    serverDish = dish;
                }
            }
            if (serverDish != null) {
                serverOrderData.put(serverDish, orderData.getValue());
            }
        }

        order.clear();
        order.setBasket(serverOrderData);

        order.setOrderState(Order.OrderState.PREPARING);
        orders.add(order);


        Message reply = new Message(MessageType.ORDER, order);
        communication.sendMessage(uid, reply);
        notifyClient();
        notifyUpdate();
    }

    /**
     * Clears the Basket requested by the Client
     * @param message : Request message from Client
     */
    private void processBasketClear(Message message) {
        User clientUser = (User) message.getPayload();
        User serverUser = null;

        for (User user : users) {
            if (user.getName().equals(clientUser.getName())) {
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
    private void processOrderCancel(Message message) {
        Order clientOrder = (Order) message.getPayload();

        for (Order order : orders) {
            if (order.getUser().getName().equals(clientOrder.getUser().getName())) {
                order.cancelOrder();
                notifyUpdate();
            }
        }
    }

    /**
     * Adds listener to be updated when notifyUpdate() is called
     * @param listener An update listener to be informed of all model changes.
     */
    @Override
    public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Called when a model is changed to inform all models of this change so they can update.
     */
    @Override
    public void notifyUpdate() {
        for (UpdateListener listener : listeners) {
            listener.updated(new UpdateEvent());
        }
    }

    /**
     * Sends a server message telling the Client to update. Called after a change on the server is made such as
     * adding a new Dish to the restaurant.
     */
    private void notifyClient() {
        if (communication != null) {
            communication.sendMessage(new Message(MessageType.UPDATE));
        }
    }
}
