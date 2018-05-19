package common;

import server.ServerInterface;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class to enable common methods to access methods in addition to those in ServerInterface, as ServerApplication isn't in package scope.
 * @author Oscar van Leusen
 */
public class Server implements ServerInterface {
    private ServerInterface server;
    private StockManager stockManager;
    private ArrayList<User> users;
    private ArrayList<Order> orders;

    public Server(ServerInterface server, StockManager stockManager, ArrayList<User> users, ArrayList<Order> orders) {
        this.server = server;
        this.stockManager = stockManager;
        this.users = users;
        this.orders = orders;
    }

    /**
     * Adds a user to the server
     * @param user : newly created User
     */
    public void addUser(User user) {
        users.add(user);
        server.notifyUpdate();
    }

    /**
     * Adds an order to the server
     * @param order : Order to add to the server
     */
    public void addOrder(Order order) {
        orders.add(order);
        server.notifyUpdate();
    }

    /**
     * Gets the StockManager's Dish and Ingredient stock.
     * @return List of StockManager's StockItems
     */
    public List<StockItem> getStock() {
        return stockManager.getStock();
    }

    //All interface methods, simply call the ServerApplication's methods.

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void loadConfiguration(String filename) throws FileNotFoundException {
        server.loadConfiguration(filename);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        server.setRestockingIngredientsEnabled(enabled);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        server.setRestockingDishesEnabled(enabled);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void setStock(Dish dish, Number stock) {
        server.setStock(dish, stock);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        server.setStock(ingredient, stock);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<Dish> getDishes() {
        return server.getDishes();
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        return server.addDish(name, description, price, restockThreshold, restockAmount);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeDish(Dish dish) throws UnableToDeleteException {
        server.removeDish(dish);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        server.addIngredientToDish(dish, ingredient, quantity);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        server.removeIngredientFromDish(dish, ingredient);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        server.setRecipe(dish, recipe);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        server.setRestockLevels(dish, restockThreshold, restockAmount);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getRestockThreshold(Dish dish) {
        return server.getRestockThreshold(dish);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getRestockAmount(Dish dish) {
        return server.getRestockAmount(dish);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return server.getRecipe(dish);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return server.getDishStockLevels();
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<Ingredient> getIngredients() {
        return server.getIngredients();
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        return server.addIngredient(name, unit, supplier, restockThreshold, restockAmount);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
        server.removeIngredient(ingredient);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        server.setRestockLevels(ingredient, restockThreshold, restockAmount);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        return server.getRestockThreshold(ingredient);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return server.getRestockAmount(ingredient);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return server.getIngredientStockLevels();
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<Supplier> getSuppliers() {
        return server.getSuppliers();
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Supplier addSupplier(String name, Number distance) {
        return server.addSupplier(name, distance);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
        server.removeSupplier(supplier);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return server.getSupplierDistance(supplier);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<Drone> getDrones() {
        return server.getDrones();
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Drone addDrone(Number speed) {
        return server.addDrone(speed);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        server.removeDrone(drone);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getDroneSpeed(Drone drone) {
        return server.getDroneSpeed(drone);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public String getDroneStatus(Drone drone) {
        return server.getDroneStatus(drone);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<Staff> getStaff() {
        return server.getStaff();
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Staff addStaff(String name) {
        return server.addStaff(name);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        server.removeStaff(staff);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public String getStaffStatus(Staff staff) {
        return server.getStaffStatus(staff);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<Order> getOrders() {
        return server.getOrders();
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {
        server.removeOrder(order);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getOrderDistance(Order order) {
        return server.getOrderDistance(order);
    }
    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public boolean isOrderComplete(Order order) {
        return server.isOrderComplete(order);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public String getOrderStatus(Order order) {
        return server.getOrderStatus(order);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public Number getOrderCost(Order order) {
        return server.getOrderCost(order);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<Postcode> getPostcodes() {
        return server.getPostcodes();
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void addPostcode(String code, Number distance) {
        server.addPostcode(code, distance);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        server.removePostcode(postcode);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public List<User> getUsers() {
        return server.getUsers();
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        server.removeUser(user);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void addUpdateListener(UpdateListener listener) {
        server.addUpdateListener(listener);
    }

    /**
     * Calls the associated method from ServerApplication.
     */
    @Override
    public void notifyUpdate() {
        server.notifyUpdate();
    }
}
