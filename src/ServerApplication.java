import common.*;
import exception.*;
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
public class ServerApplication implements ServerInterface {

    private ServerWindow serverWindow;
    private CommsServer communication;

    //HashMap linking a dish (key) to StockItem (value).
    HashMap<Dish, StockItem> dishStock = new HashMap<>();
    //HashMap linking an ingredient (key) to StockItem (value).
    HashMap<Ingredient, StockItem> ingredientStock = new HashMap<>();

    private List<Supplier> suppliers;
    private List<Ingredient> ingredients;
    private List<Dish> dishes;
    private List<Postcode> postcodes;
    private List<User> users;
    private List<Order> orders;
    private List<StockItem> stock;
    private List<Staff> staff;
    private List<Drone> drones;

    public static void main(String args[]) {
        ServerInterface serverInterface = initialise();
        ServerApplication app = (ServerApplication) serverInterface;
        ServerWindow window = app.launchGUI(serverInterface);
    }

    private static ServerInterface initialise() {
        ServerApplication app = new ServerApplication();

        try {
            app.communication = new CommsServer(app, 5000);
        } catch (IOException e) {
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
            supplierLines = fileStream
                                .filter(line -> line.startsWith("SUPPLIER:"))
                                .collect(Collectors.toList());
            ingredientLines = fileStream
                                .filter(line -> line.startsWith("INGREDIENT:"))
                                .collect(Collectors.toList());
            dishLines = fileStream
                                .filter(line -> line.startsWith("DISH:"))
                                .collect(Collectors.toList());
            postcodeLines = fileStream
                                .filter(line -> line.startsWith("POSTCODE:"))
                                .collect(Collectors.toList());
            userLines = fileStream
                                .filter(line -> line.startsWith("USER:"))
                                .collect(Collectors.toList());
            orderLines = fileStream
                                .filter(line -> line.startsWith("ORDER:"))
                                .collect(Collectors.toList());
            stockLines = fileStream
                                .filter(line -> line.startsWith("STOCK:"))
                                .collect(Collectors.toList());
            staffLines = fileStream
                                .filter(line -> line.startsWith("STAFF:"))
                                .collect(Collectors.toList());
            droneLines = fileStream
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
    private void loadIngredients(List<String> ingredientLines) throws InvalidSupplierException, InvalidStockItemException {
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
            StockItem stockStore = new StockItem(ingredient, 0, Long.parseLong(lineParse[4]), Long.parseLong(lineParse[5]));
            ingredientStock.put(ingredient, stockStore);
            ingredients.add(ingredient);
            stock.add(stockStore);
        }
    }

    /**
     * Instantiates all Dishes from the text representation of Dishes in the config file.
     * @param dishLines : List containing non-parsed lines of dish information.
     */
    private void loadDishes(List<String> dishLines) throws InvalidStockItemException, InvalidIngredientException {
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
                String[] currentIngredient = ingredientParse[i].split(" * ");
                //Looks for an existing ingredient matching the name of the ingredient to add
                Ingredient dishIngredient = null;
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.getName().equals(currentIngredient[1])) {
                        dishIngredient = ingredient;
                    }
                }
                if (dishIngredient == null) {
                    throw new InvalidIngredientException("Non-valid ingredient entered for dish: " + lineParse[1] + " when reading Configuration file.");
                }
                dish.addIngredient(dishIngredient, Float.parseFloat(currentIngredient[0]));
            }
            dishStock.put(dish, stockStore);
            dishes.add(dish);
            stock.add(stockStore);
        }
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
                String[] orderInfo = orderContents[i].split(" * ");
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
        StockManager staffStockManager = new StockManager(dishStock, ingredientStock);
        for (String line : staffLines) {
            //Structure: [0]STAFF:[1]Name
            String[] lineParse = line.split(":");
            Staff staffMember = new Staff(lineParse[1], staffStockManager);
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

    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {

    }

    @Override
    public void setStock(Dish dish, Number stock) {

    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {

    }

    @Override
    public List<Dish> getDishes() {
        return null;
    }

    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        return null;
    }

    @Override
    public void removeDish(Dish dish) throws UnableToDeleteException {

    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {

    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {

    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {

    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {

    }

    @Override
    public Number getRestockThreshold(Dish dish) {
        return null;
    }

    @Override
    public Number getRestockAmount(Dish dish) {
        return null;
    }

    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return null;
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return null;
    }

    @Override
    public List<Ingredient> getIngredients() {
        return null;
    }

    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        return null;
    }

    @Override
    public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {

    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {

    }

    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        return null;
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return null;
    }

    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return null;
    }

    @Override
    public List<Supplier> getSuppliers() {
        return null;
    }

    @Override
    public Supplier addSupplier(String name, Number distance) {
        return null;
    }

    @Override
    public void removeSupplier(Supplier supplier) throws UnableToDeleteException {

    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return null;
    }

    @Override
    public List<Drone> getDrones() {
        return null;
    }

    @Override
    public Drone addDrone(Number speed) {
        return null;
    }

    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {

    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return null;
    }

    @Override
    public String getDroneStatus(Drone drone) {
        return null;
    }

    @Override
    public List<Staff> getStaff() {
        return null;
    }

    @Override
    public Staff addStaff(String name) {
        return null;
    }

    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {

    }

    @Override
    public String getStaffStatus(Staff staff) {
        return null;
    }

    @Override
    public List<Order> getOrders() {
        return null;
    }

    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {

    }

    @Override
    public Number getOrderDistance(Order order) {
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
    public List<Postcode> getPostcodes() {
        return null;
    }

    @Override
    public void addPostcode(String code, Number distance) {

    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {

    }

    @Override
    public List<User> getUsers() {
        return null;
    }

    @Override
    public void removeUser(User user) throws UnableToDeleteException {

    }

    @Override
    public void addUpdateListener(UpdateListener listener) {

    }

    @Override
    public void notifyUpdate() {

    }
}
