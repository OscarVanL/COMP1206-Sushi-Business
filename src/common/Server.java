package common;

import server.ServerInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class to enable common methods to access methods in addition to those in ServerInterface, as ServerApplication isn't in package scope.
 * @author Oscar van Leusen
 */
public class Server implements ServerInterface {
    ServerInterface server;
    StockManager stockManager;
    List<User> users;
    List<Order> orders;

    public Server(ServerInterface server, StockManager stockManager, List<User> users, List<Order> orders) {
        this.server = server;
        this.stockManager = stockManager;
        this.users = users;
        this.orders = orders;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<StockItem> getStock() {
        return stockManager.getStock();
    }

    //All interface methods, simply call the ServerApplication's methods.

    @Override
    public void loadConfiguration(String filename) {
        server.loadConfiguration(filename);
    }

    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        server.setRestockingIngredientsEnabled(enabled);
    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        server.setRestockingDishesEnabled(enabled);
    }

    @Override
    public void setStock(Dish dish, Number stock) {
        server.setStock(dish, stock);
    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        server.setStock(ingredient, stock);
    }

    @Override
    public List<Dish> getDishes() {
        return server.getDishes();
    }

    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        return server.addDish(name, description, price, restockThreshold, restockAmount);
    }

    @Override
    public void removeDish(Dish dish) throws UnableToDeleteException {
        server.removeDish(dish);
    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        server.addIngredientToDish(dish, ingredient, quantity);
    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        server.removeIngredientFromDish(dish, ingredient);
    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        server.setRecipe(dish, recipe);
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        server.setRestockLevels(dish, restockThreshold, restockAmount);
    }

    @Override
    public Number getRestockThreshold(Dish dish) {
        return server.getRestockThreshold(dish);
    }

    @Override
    public Number getRestockAmount(Dish dish) {
        return server.getRestockAmount(dish);
    }

    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return server.getRecipe(dish);
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return server.getDishStockLevels();
    }

    @Override
    public List<Ingredient> getIngredients() {
        return server.getIngredients();
    }

    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        return server.addIngredient(name, unit, supplier, restockThreshold, restockAmount);
    }

    @Override
    public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
        server.removeIngredient(ingredient);
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        server.setRestockLevels(ingredient, restockThreshold, restockAmount);
    }

    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        return server.getRestockThreshold(ingredient);
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return server.getRestockAmount(ingredient);
    }

    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return server.getIngredientStockLevels();
    }

    @Override
    public List<Supplier> getSuppliers() {
        return server.getSuppliers();
    }

    @Override
    public Supplier addSupplier(String name, Number distance) {
        return server.addSupplier(name, distance);
    }

    @Override
    public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
        server.removeSupplier(supplier);
    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return server.getSupplierDistance(supplier);
    }

    @Override
    public List<Drone> getDrones() {
        return server.getDrones();
    }

    @Override
    public Drone addDrone(Number speed) {
        return server.addDrone(speed);
    }

    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        server.removeDrone(drone);
    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return server.getDroneSpeed(drone);
    }

    @Override
    public String getDroneStatus(Drone drone) {
        return server.getDroneStatus(drone);
    }

    @Override
    public List<Staff> getStaff() {
        return server.getStaff();
    }

    @Override
    public Staff addStaff(String name) {
        return server.addStaff(name);
    }

    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        server.removeStaff(staff);
    }

    @Override
    public String getStaffStatus(Staff staff) {
        return server.getStaffStatus(staff);
    }

    @Override
    public List<Order> getOrders() {
        return server.getOrders();
    }

    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {
        server.removeOrder(order);
    }

    @Override
    public Number getOrderDistance(Order order) {
        return server.getOrderDistance(order);
    }

    @Override
    public boolean isOrderComplete(Order order) {
        return server.isOrderComplete(order);
    }

    @Override
    public String getOrderStatus(Order order) {
        return server.getOrderStatus(order);
    }

    @Override
    public Number getOrderCost(Order order) {
        return server.getOrderCost(order);
    }

    @Override
    public List<Postcode> getPostcodes() {
        return server.getPostcodes();
    }

    @Override
    public void addPostcode(String code, Number distance) {
        server.addPostcode(code, distance);
    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        server.removePostcode(postcode);
    }

    @Override
    public List<User> getUsers() {
        return server.getUsers();
    }

    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        server.removeUser(user);
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        server.addUpdateListener(listener);
    }

    @Override
    public void notifyUpdate() {
        server.notifyUpdate();
    }
}
