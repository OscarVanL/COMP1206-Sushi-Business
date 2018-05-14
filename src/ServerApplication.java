import common.*;
import exceptions.*;
import server.ServerInterface;
import server.ServerWindow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Oscar van Leusen
 */
public class ServerApplication extends Thread implements ServerInterface {

    private ServerWindow serverWindow;
    private Configuration config;
    private CommsServer communication;

    private StockManager stockManager = new StockManager();

    private ArrayList<Supplier> suppliers = new ArrayList<>();
    private ArrayList<Ingredient> ingredients = new ArrayList<>();
    private ArrayList<Dish> dishes = new ArrayList<>();
    private ArrayList<Postcode> postcodes = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Order> orders = new ArrayList<>();
    //private ArrayList<StockItem> stock = new ArrayList<>();
    private ArrayList<Staff> staff = new ArrayList<>();
    private ArrayList<Drone> drones = new ArrayList<>();

    public static boolean ingredientsRestocked = true;
    public static boolean dishesRestocked = true;

    public static void main(String args[]) {
        ServerInterface serverInterface = initialise();
        ServerApplication app = (ServerApplication) serverInterface;
        app.serverWindow = app.launchGUI(serverInterface);
        try {
            app.communication = new CommsServer(app, 5000);
            //Starts threaded aspect of ServerApplication that checks for messages in CommsServer.
            app.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ServerInterface initialise() {
        ServerApplication app = new ServerApplication();
        app.loadConfiguration("ConfigurationExample.txt");
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
    public void loadConfiguration(String filename) {
        try {
            Server server = new Server(this, stockManager, users, orders);
            config = new Configuration(server, filename);
            config.loadConfiguration();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        try {
            stockManager.setStockLevel(ingredient, stock);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
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
    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        dish.addIngredient(ingredient, quantity);
    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        dish.removeIngredient(ingredient);
    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        dish.setRecipe(recipe);
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        try {
            stockManager.setRestockAmount(dish, restockAmount);
            stockManager.setRestockThreshold(dish, restockThreshold);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
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
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        try {
            stockManager.setRestockThreshold(ingredient, restockThreshold);
            stockManager.setRestockAmount(ingredient, restockAmount);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
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
        return newDrone;
    }

    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        drones.remove(drone);
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
        return newStaff;
    }

    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        this.staff.remove(staff);
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
        orders.remove(order);
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
            return "BASKET";
        } else if (state == Order.OrderState.PREPARING) {
            return "PREPARING";
        } else if (state == Order.OrderState.DELIVERING) {
            return "DELIVERING";
        } else {
            return "COMPLETE";
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
    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        if (postcode.isDeleteSafe()) {
            postcodes.remove(postcode);
        } else {
            throw new UnableToDeleteException("Attempted to delete Postcode while not safe to delete Postcode");
        }
    }

    @Override
    public List<User> getUsers() {
        return users;
    }

    public User addUser(String username, String password, String location, Postcode postcode) {
        User newUser = new User(username, password, location, postcode);
        users.add(newUser);
        return newUser;
    }

    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        if (user.isDeleteSafe()) {
            users.remove(user);
        } else {
            throw new UnableToDeleteException("Attempted to delete User while not safe to delete user.");
        }

    }

    @Override
    public void run() {
        try {
            synchronized(this) {
                while (!communication.getMessageStatus()) {
                    this.wait();
                }
                Message message = communication.receiveMessage();
                processMessage(message);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
        /**} else if (type == MessageType.GET_DISH_DESC) {
            processGetDishDesc(message);**/
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
        } else if (type == MessageType.SEND_DISH) {
            processAddDishToBasket(message);
        } else if (type == MessageType.SEND_CHECKOUT) {
            processUserCheckout(message);
        } else if (type == MessageType.SEND_CLEAR) {
            processBasketClear(message);
        } else if (type == MessageType.SEND_CANCEL) {
            processOrderCancel(message);
        }
    }

    private void processRegister(Message message) {
        int uid = message.getConnectionUID();
        User newUser = (User) message.getPayload();
        newUser.setClientUID(uid);
        users.add(newUser);
        Message reply = new Message(MessageType.REGISTER_SUCCESS, true);
        communication.sendMessage(uid, reply);
    }

    private void processLogin(Message message) {
        int uid = message.getConnectionUID();
        Message reply;
        ArrayList<String> loginDetails = (ArrayList<String>) message.getPayload();
        boolean loginCorrect = false;
        for (User user : users) {
            //there's a user where both username and password match those entered, it was a correct login!
            if (user.getUsername().equals(loginDetails.get(0)) && user.passwordMatches(loginDetails.get(1))) {
                loginCorrect = true;
            }
        }
        if (loginCorrect) {
            reply = new Message(MessageType.LOGIN_SUCCESS, true);
        } else {
            reply = new Message(MessageType.LOGIN_SUCCESS, false);
        }
        communication.sendMessage(uid, reply);
    }

    private void processGetPostcodes(Message message) {
        int uid = message.getConnectionUID();
        Message reply = new Message(MessageType.POSTCODES, this.getPostcodes());
        communication.sendMessage(uid, reply);
    }

    public void processGetDishes(Message message) {
        int uid = message.getConnectionUID();
        Message reply = new Message(MessageType.DISHES, this.getDishes());
        communication.sendMessage(uid, reply);
    }

    //TODO: Same as other TODO, how does this work for objects passed to/from JREs with sockets.
    /**public void processGetDishDesc(Message message) {
        int uid = message.getConnectionUID();
        Dish dishDescRequest = (Dish) message.getPayload();
        Message reply = new Message(MessageType.DISH_DESC, dishDescRequest.getDishDescription());
        communication.sendMessage(uid, reply);
    }**/

    /**public void processGetDishPrice(Message message) {
        int uid = message.getConnectionUID();
        Dish dishPriceRequest = (Dish) message.getPayload();
        Message reply = new Message(MessageType.DISH_PRICE, dishPriceRequest.getPrice());
        communication.sendMessage(uid, reply);
    }**/

    public void processGetBasket(Message message) {
        int uid = message.getConnectionUID();
        User user = (User) message.getPayload();

        Message reply = new Message(MessageType.BASKET, null);
        for (Order order : orders) {
            //If any Order object that is still in basket and for the requested user, this is the correct Order.
            if (order.getOrderState() == Order.OrderState.BASKET && order.getUser().equals(user)) {
                reply = new Message(MessageType.BASKET, order);
            }
        }
        communication.sendMessage(uid, reply);

    }

    public void processGetBasketCost(Message message) {
        int uid = message.getConnectionUID();
        User user = (User) message.getPayload();

        Message reply = new Message(MessageType.BASKET_COST, null);
        for (Order order : orders) {
            if (order.getUser().equals(user)) {
                reply = new Message(MessageType.BASKET_COST, order.getOrderPrice());
            }
        }
        communication.sendMessage(uid, reply);
    }

    public void processGetOrders(Message message) {
        int uid = message.getConnectionUID();
        Message reply = new Message(MessageType.ORDERS, orders);
        communication.sendMessage(uid, reply);
    }

    public void processGetOrderStatus(Message message) {
        int uid = message.getConnectionUID();
        Order orderRequested = (Order) message.getPayload();
        String status;
        Order.OrderState state = orderRequested.getOrderState();
        if (state == Order.OrderState.BASKET) {
            status = "BASKET";
        } else if (state == Order.OrderState.PREPARING) {
            status = "PREPARING";
        } else if (state == Order.OrderState.DELIVERING) {
            status = "DELIVERING";
        } else if (state == Order.OrderState.COMPLETE) {
            status = "COMPLETE";
        } else if (state == Order.OrderState.CANCELLED) {
            status = "CANCELLED";
        }else {
            status = "";
        }
        Message reply = new Message(MessageType.STATUS, status);
        communication.sendMessage(uid, reply);
    }

    public void processGetOrderCost(Message message) {
        int uid = message.getConnectionUID();
        Order orderRequested = (Order) message.getPayload();
        Message reply = new Message(MessageType.COST, orderRequested.getOrderPrice());
        communication.sendMessage(uid, reply);
    }

    public void processAddDishToBasket(Message message) {
        int uid = message.getConnectionUID();
        //Structure: [0]User user, [1]Dish dish, [2] Number quantity
        ArrayList<Object> dishData = (ArrayList<Object>) message.getPayload();
        for (Order order : orders) {
            if (order.getUser().equals(dishData.get(0))) {
                order.addDish((Dish) dishData.get(1), (int) dishData.get(2));
            }
        }

    }

    public void processUserCheckout(Message message) {
        int uid = message.getConnectionUID();
        User user = (User) message.getPayload();
        Message reply = new Message(MessageType.ORDER, null);
        for (Order order : orders) {
            if (order.getUser().equals(user)) {
                order.setOrderState(Order.OrderState.PREPARING);
                reply = new Message(MessageType.ORDER, order);
            }
        }
        communication.sendMessage(uid, reply);
    }

    public void processBasketClear(Message message) {
        int uid = message.getConnectionUID();
        User user = (User) message.getPayload();
        for (Order order : orders) {
            if (order.getUser().equals(user)) {
                order.clear();
            }
        }
    }

    public void processOrderCancel(Message message) {
        int uid = message.getConnectionUID();
        //TODO : Fairly sure this doesn't work, and the same logical flaw also applies in multiple methods above too. I don't know how objects link and whether they continue to be .equals() after being serialised and passed along the Socket to the other application. Must do research on this.
        Order toCancel = (Order) message.getPayload();
        toCancel.cancelOrder();
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {

    }

    @Override
    public void notifyUpdate() {

    }
}
