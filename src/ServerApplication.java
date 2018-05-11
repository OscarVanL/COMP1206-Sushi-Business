import common.*;
import server.ServerInterface;
import server.ServerWindow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ServerApplication implements ServerInterface {

    private ServerWindow serverWindow;
    CommsServer communication;

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

    @Override
    public void loadConfiguration(String filename) throws FileNotFoundException {

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
