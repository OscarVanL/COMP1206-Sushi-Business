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
    private CommsServer communication;

    private StockManager stockManager = new StockManager();

    private ArrayList<Supplier> suppliers = new ArrayList<>();
    private ArrayList<Ingredient> ingredients = new ArrayList<>();
    private ArrayList<Dish> dishes = new ArrayList<>();
    private ArrayList<Postcode> postcodes = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Order> orders = new ArrayList<>();
    private ArrayList<StockItem> stock = new ArrayList<>();
    private ArrayList<Staff> staff = new ArrayList<>();
    private ArrayList<Drone> drones = new ArrayList<>();

    public static boolean ingredientsRestocked = true;
    public static boolean dishesRestocked = true;

    public static void main(String args[]) {
        ServerInterface serverInterface = initialise();
        ServerApplication app = (ServerApplication) serverInterface;
        ServerWindow window = app.launchGUI(serverInterface);
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
        try {
            app.loadConfiguration("ConfigurationExample.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidSupplierException e) {
            e.printStackTrace();
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        } catch (InvalidIngredientException e) {
            e.printStackTrace();
        } catch (InvalidPostcodeException e) {
            e.printStackTrace();
        } catch (InvalidUserException e) {
            e.printStackTrace();
        } catch (InvalidDishException e) {
            e.printStackTrace();
        }
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
    public void loadConfiguration(String filename) throws FileNotFoundException, InvalidSupplierException, InvalidStockItemException, InvalidIngredientException, InvalidPostcodeException, InvalidUserException, InvalidDishException {
        List<String> allLines = new ArrayList<>();
        List<String> supplierLines = new ArrayList<>();
        List<String> ingredientLines = new ArrayList<>();
        List<String> dishLines = new ArrayList<>();
        List<String> postcodeLines = new ArrayList<>();
        List<String> userLines = new ArrayList<>();
        List<String> orderLines = new ArrayList<>();
        List<String> stockLines = new ArrayList<>();
        List<String> staffLines = new ArrayList<>();
        List<String> droneLines = new ArrayList<>();
        try (Stream<String> fileStream = Files.lines(Paths.get(filename))) {
            allLines = fileStream.collect(Collectors.toList());
            supplierLines = allLines.stream()
                                .filter(line -> line.startsWith("SUPPLIER:"))
                                .collect(Collectors.toList());
            ingredientLines = allLines.stream()
                                .filter(line -> line.startsWith("INGREDIENT:"))
                                .collect(Collectors.toList());
            dishLines = allLines.stream()
                                .filter(line -> line.startsWith("DISH:"))
                                .collect(Collectors.toList());
            postcodeLines = allLines.stream()
                                .filter(line -> line.startsWith("POSTCODE:"))
                                .collect(Collectors.toList());
            userLines = allLines.stream()
                                .filter(line -> line.startsWith("USER:"))
                                .collect(Collectors.toList());
            orderLines = allLines.stream()
                                .filter(line -> line.startsWith("ORDER:"))
                                .collect(Collectors.toList());
            stockLines = allLines.stream()
                                .filter(line -> line.startsWith("STOCK:"))
                                .collect(Collectors.toList());
            staffLines = allLines.stream()
                                .filter(line -> line.startsWith("STAFF:"))
                                .collect(Collectors.toList());
            droneLines = allLines.stream()
                                .filter(line -> line.startsWith("DRONE:"))
                                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException();
        }

        loadSuppliers(supplierLines);
        loadIngredients(ingredientLines);
        loadDishes(dishLines);
        loadPostcodes(postcodeLines);
        loadUsers(userLines);
        loadOrders(orderLines);
        loadStock(stockLines);
        loadStaff(staffLines);
        loadDrones(droneLines);
    }

    /**
     * Instantiates all Suppliers from the text representation of suppliers from the config file.
     * @param supplierLines : List containing non-parsed lines of supplier information.
     */
    private void loadSuppliers(List<String> supplierLines) {
        //Adds all suppliers in the Configuration structure to our Suppliers List
        for (String line : supplierLines) {
            //Structure: [0]SUPPLIER:[1]Name:[2]Distance
            String[] lineParse = line.split(":");
            Supplier supplier = new Supplier(lineParse[1], Integer.parseInt(lineParse[2]));
            suppliers.add(supplier);
        }
    }

    /**
     * Instantiates all Ingredients and their Stock data from the text representation of Ingredients from the Config file
     * @param ingredientLines : List containing non-parsed lines of Ingredient information.
     */
    private void loadIngredients(List<String> ingredientLines) throws InvalidSupplierException {
        //HashMap linking an ingredient (key) to StockItem (value).
        HashMap<Ingredient, StockItem> ingredientStock = new HashMap<>();

        //Adds all ingredients in the Configuration structure to our Suppliers array
        for (String line : ingredientLines) {
            //Stucture: [0]INGREDIENT:[1]Name:[2]Unit:[3]Supplier:[4]Restock Threshold:[5]Restock Amount
            String[] lineParse = line.split(":");
            Supplier ingredientSupplier = null;
            //Finds supplier matching given name
            for (Supplier supplier : suppliers) {
                if (supplier.getName().equals(lineParse[3])) {
                    ingredientSupplier = supplier;
                }
            }
            if (ingredientSupplier == null) {
                throw new InvalidSupplierException("Non-valid supplier entered for ingredient when reading Configuration file");
            }
            Ingredient ingredient = new Ingredient(lineParse[1], lineParse[2], ingredientSupplier);
            StockItem stockStore = null;
            try {
                stockStore = new StockItem(ingredient, 0, Long.parseLong(lineParse[4]), Long.parseLong(lineParse[5]));
            } catch (InvalidStockItemException e) {
                e.printStackTrace();
            }
            ingredientStock.put(ingredient, stockStore);
            ingredients.add(ingredient);
            stock.add(stockStore);
        }
        stockManager.addIngredients(ingredientStock);
    }

    /**
     * Instantiates all Dishes from the text representation of Dishes in the config file.
     * @param dishLines : List containing non-parsed lines of dish information.
     */
    private void loadDishes(List<String> dishLines) throws InvalidStockItemException, InvalidIngredientException {
        //HashMap linking a dish (key) to StockItem (value).
        HashMap<Dish, StockItem> dishStock = new HashMap<>();

        for (String line : dishLines) {
            //Structure: [0]DISH:[1]Name:[2]Description:[3]Price:[4]Restock Threshold:[5]Restock Amount:[6]Quantity * Item,Quantity * Item...
            String[] lineParse = line.split(":");
            Dish dish = new Dish(lineParse[1], lineParse[2], Long.parseLong(lineParse[3]));
            StockItem stockStore = new StockItem(dish, 0, Integer.parseInt(lineParse[4]), Integer.parseInt(lineParse[5]));

            //Parse each Ingredient (separated by a comma). [0]ingredient 1, [1]ingredient 2, ...
            String[] ingredientParse = lineParse[6].split(",");

            //Iterates through each ingredient in the dish to add it.
            for (int i=0; i<ingredientParse.length; i++) {
                //Parses Ingredients into structure: [0]Quantity,[1]Name
                String[] currentIngredient = ingredientParse[i].split("\\s\\*\\s");
                //Looks for an existing ingredient matching the name of the ingredient to add
                Ingredient dishIngredient = null;
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.getName().equals(currentIngredient[1])) {
                        dishIngredient = ingredient;
                    }
                }
                if (dishIngredient == null) {
                    throw new InvalidIngredientException("Non-valid ingredient: " + currentIngredient[1] + " entered for dish: " + lineParse[1] + " when reading Configuration file.");
                }
                dish.addIngredient(dishIngredient, Float.parseFloat(currentIngredient[0]));
            }
            dishStock.put(dish, stockStore);
            dishes.add(dish);
            stock.add(stockStore);
        }

        stockManager.addDishes(dishStock);
    }

    /**
     * Instantiates all valid Postcodes from the text representation of Postcode in the config file.
     * @param postcodeLines : List containing non-parsed lines of postcode information.
     */
    private void loadPostcodes(List<String> postcodeLines) {
        for (String line : postcodeLines) {
            //Structure: [1]POSTCODE:[1]Postcode:[2]Distance
            String[] lineParse = line.split(":");
            Postcode postcode = new Postcode(lineParse[1], Long.parseLong(lineParse[2]));
            postcodes.add(postcode);
        }
    }

    /**
     * Instantiates all Users from the text representation of Users in the config file.
     * @param userLines : List containing non-parsed lines of User information.
     */
    private void loadUsers(List<String> userLines) throws InvalidPostcodeException {
        //Adds all Users in the configuration structure to our Users List.
        for (String line : userLines) {
            //Structure: [0]USER:[1]Name:[2]Password:[3]Location:[4]Postcode
            String[] lineParse = line.split(":");
            Postcode userPostcode = null;
            for (Postcode postcode : postcodes) {
                if (postcode.getName().equals(lineParse[4])) {
                    userPostcode = postcode;
                }
            }
            if (userPostcode == null) {
                throw new InvalidPostcodeException("Non-valid postcode entered for user: " + lineParse[1] + " when reading Configuration file.");
            }
            User user = new User(lineParse[1], lineParse[2], lineParse[3], userPostcode);
            users.add(user);
        }
    }

    /**
     * Instantiates all Orders from the text representation of Orders in the config file.
     * @param orderLines : List containing the non-parsed lines of Order information.
     */
    private void loadOrders(List<String> orderLines) throws InvalidUserException, InvalidDishException {
        //Adds all Orders in the configuration structure to our Orders List.
        for (String line : orderLines) {
            //Structure: [0]ORDER:[1]User:[2]Quantity * Dish,Quantity * Dish ...
            String[] lineParse = line.split(":");
            User orderUser = null;
            for (User user : users) {
                if (user.getName().equals(lineParse[0])) {
                    orderUser = user;
                }
            }
            if (orderUser == null) {
                throw new InvalidUserException("Non-valid user entered for order when reading Configuration file.");
            }
            Order order = new Order(orderUser);

            //Parse each ordered dish (separated by a comma), Structure: [0]Quantity * Dish, [1]Quantity * Dish, ...
            String[] orderContents = lineParse[2].split(",");
            for (int i=0; i<orderContents.length; i++) {
                //Structure: [0]Quantity, [1]Dish
                String[] orderInfo = orderContents[i].split("\\s\\*\\s");
                Dish orderDish = null;
                for (Dish dish : dishes) {
                    if (dish.getName().equals(orderInfo[1])) {
                        orderDish = dish;
                    }
                }
                if (orderDish == null) {
                    throw new InvalidDishException("Non-valid dish entered for " + orderUser.getName() + "'s order when reading configuration file.");
                }
                //Finally, adds the dish and quantity to the order.
                order.addDish(orderDish, Integer.parseInt(orderInfo[0]));
            }
            orders.add(order);
        }
    }

    /**
     * Instantiates all StockItem elements with their respective quantities from the Config file
     * The StockItems will have been created already when the Dishes and Ingredients were loaded previously.
     * Either Dish or Ingredient can be stocked, so both must be checked.
     * @param stockLines : List containing non-parsed lines of Stock information.
     */
    private void loadStock(List<String> stockLines) throws InvalidStockItemException {
        //Adds all quantities of StockItems from the Configuration file to our StockItems.
        for (String line : stockLines) {
            //Structure: [0]STOCK:[1]Dish|Ingredient:[2]Quantity
            String[] parseLine = line.split(":");
            Model stockedItem = null;

            //First finds whatever Dish or Ingredient is being referenced.
            for (Dish dish : dishes) {
                if (dish.getName().equals(parseLine[1])) {
                    stockedItem = dish;
                }
            }
            for (Ingredient ingredient : ingredients) {
                if (ingredient.getName().equals(parseLine[1])) {
                    stockedItem = ingredient;
                }
            }
            if (stockedItem == null) {
                throw new InvalidStockItemException("Non-valid Dish or Ingredient entered for stock in Configuration file");
            }

            //Then finds whatever StockItem this dish/ingredient is contained in
            if (stockedItem != null) {
                for (StockItem item : stock) {
                    if (item.getStockedItem().equals(stockedItem)) {
                        item.setStock(Long.parseLong(parseLine[2]));
                    }
                }
            }
        }
    }

    /**
     * Instantiates all Staff elements with their respective names from the Config file
     * @param staffLines : List containing non-parsed lines of Staff details.
     */
    private void loadStaff(List<String> staffLines) {
        for (String line : staffLines) {
            //Structure: [0]STAFF:[1]Name
            String[] lineParse = line.split(":");
            Staff staffMember = new Staff(lineParse[1], stockManager);
            staff.add(staffMember);
        }
    }


    /**
     * Instantiates all Drone elements with their respective speed from the Config file
     * @param droneLines : List containing non-parsed lines of Drone speeds.
     */
    private void loadDrones(List<String> droneLines) {
        for (String line : droneLines) {
            //Structure: [0]DRONE:[1]Speed
            String[] lineParse = line.split(":");
            Drone drone = new Drone(Integer.parseInt(lineParse[1]));
            drones.add(drone);
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
            stock.add(newDishStock);
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
        stock.add(newIngredientStock);
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
