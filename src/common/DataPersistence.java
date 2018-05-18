package common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Oscar van Leusen
 */
public class DataPersistence extends Thread {
    File folder = new File("/");
    FileWriter writer;
    Server server;
    StockManager stockManager;
    List<Supplier> suppliers;
    List<Ingredient> ingredients;
    List<Dish> dishes;
    List<Postcode> postcodes;
    List<User> users;
    List<Order> orders;
    List<Staff> staff;
    List<Drone> drones;

    public DataPersistence(Server server, StockManager stockManager) throws IOException {
        this.server = server;
        this.stockManager = stockManager;
    }

    @Override
    public void run() {
        while (true) {
            //Ensures there are only 5 backups at a time. If there are more than 5 then existing backups are removed.
            if (countBackups() > 5) {
                removeExcessBackups();
            }
            //Makes a backup every 60 seconds.
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.suppliers = server.getSuppliers();
            this.ingredients = server.getIngredients();
            this.dishes = server.getDishes();
            this.postcodes = server.getPostcodes();
            this.users = server.getUsers();
            this.orders = server.getOrders();
            this.staff = server.getStaff();
            this.drones = server.getDrones();

            List<String> restaurantState = parseToStrings();
            for (String string : restaurantState) {
                try {
                    writer = new FileWriter(new File("Sushi-Backup-" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())));
                    writer.write(string);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int countBackups() {
        File[] filesInFolder = folder.listFiles();
        int numBackups = 0;
        for (int i=0; i<filesInFolder.length; i++) {
            if (filesInFolder[0].isFile() && filesInFolder[i].getName().contains("Sushi-Backup")) {
                numBackups++;
            }
        }
        return numBackups;
    }

    private void removeExcessBackups() {
        File oldest = null;

        long lastModified = Long.MAX_VALUE;
        for (File backup : folder.listFiles()) {
            if (backup.getName().startsWith("Sushi-Backup")) {
                if (backup.lastModified() < lastModified) {
                    oldest = backup;
                    lastModified = backup.lastModified();
                }
            }
        }
        System.out.println("Deleting old backup: " + oldest.getName());
        oldest.delete();
    }

    private List<String> parseToStrings() {
        List<String> finalOutput = new ArrayList<>();
        finalOutput.addAll(parseSuppliers());
        finalOutput.addAll(parseIngredients());
        finalOutput.addAll(parseDishes());
        finalOutput.addAll(parsePostcodes());
        finalOutput.addAll(parseUsers());
        finalOutput.addAll(parseStaff());
        finalOutput.addAll(parseDrones());
        finalOutput.addAll(parseOrders());
        finalOutput.addAll(parseStock());
        return finalOutput;
    }

    private List<String> parseSuppliers() {
        List<String> supplierOutput = new ArrayList<>();
        StringBuilder sb;
        for (Supplier supplier : suppliers) {
            sb = new StringBuilder();
            sb.append("SUPPLIER:");
            sb.append(supplier.getName());
            sb.append(":");
            sb.append(supplier.getDistance());
            sb.append("\n");
            supplierOutput.add(sb.toString());
        }
        return supplierOutput;
    }

    private List<String> parseIngredients() {
        List<String> ingredientsOutput = new ArrayList<>();
        StringBuilder sb;
        for (Ingredient ingredient : ingredients) {
            sb = new StringBuilder();
            sb.append("INGREDIENT:");
            sb.append(ingredient.getName());
            sb.append(":");
            sb.append(ingredient.getUnit());
            sb.append(":");
            sb.append(ingredient.getSupplier().getName());
            sb.append(":");
            sb.append(ingredient.getRestockThreshold());
            sb.append(":");
            sb.append(ingredient.getRestockAmount());
            sb.append("\n");
            ingredientsOutput.add(sb.toString());
        }
        return ingredientsOutput;
    }

    private List<String> parseDishes() {
        List<String> dishesOutput = new ArrayList<>();
        StringBuilder sb;
        for (Dish dish : dishes) {
            sb = new StringBuilder();
            sb.append("DISH:");
            sb.append(dish.getName());
            sb.append(":");
            sb.append(dish.getDishDescription());
            sb.append(":");
            sb.append(dish.dishPrice());
            sb.append(":");
            sb.append(dish.getRestockThreshold());
            sb.append(":");
            sb.append(dish.getRestockAmount());
            sb.append(":");
            //Parses ingredients and quantities for the dish.
            Map<Ingredient, Number> recipe = dish.getRecipe();
            for (Map.Entry<Ingredient, Number> dishIngredient : recipe.entrySet()) {
                Ingredient ingredient = dishIngredient.getKey();
                Long quantity = dishIngredient.getValue().longValue();
                sb.append(quantity + " * " + ingredient.getName()+",");
            }
            //Removes the last extra comma separating ingredients (as there is no following ingredient)
            sb.setLength(sb.length() - 1);
            sb.append("\n");
            dishesOutput.add(sb.toString());
        }
        return dishesOutput;
    }

    private List<String> parsePostcodes() {
        List<String> postcodesOutput = new ArrayList<>();
        StringBuilder sb;
        for (Postcode postcode : postcodes) {
            sb = new StringBuilder();
            sb.append("POSTCODE:");
            sb.append(postcode.getName());
            sb.append(":");
            sb.append(postcode.getDistance());
            sb.append("\n");
            postcodesOutput.add(sb.toString());
        }
        return postcodesOutput;
    }

    private List<String> parseUsers() {
        List<String> usersOutput = new ArrayList<>();
        StringBuilder sb;
        for (User user : users) {
            sb = new StringBuilder();
            sb.append("USER:");
            sb.append(user.getName());
            sb.append(":");
            sb.append(user.getPassword());
            sb.append(":");
            sb.append(user.getAddress());
            sb.append(":");
            sb.append(user.getPostcode().getName());
            sb.append("\n");
            usersOutput.add(sb.toString());
        }
        return usersOutput;
    }

    private List<String> parseStaff() {
        List<String> staffOutput = new ArrayList<>();
        StringBuilder sb;
        for (Staff staff : staff) {
            sb = new StringBuilder();
            sb.append("STAFF:");
            sb.append(staff.getName());
            staffOutput.add(sb.toString());
            sb.append("\n");
        }
        return staffOutput;
    }

    private List<String> parseDrones() {
        List<String> droneOutput = new ArrayList<>();
        StringBuilder sb;
        for (Drone drone : drones) {
            sb = new StringBuilder();
            sb.append("DRONE:");
            sb.append(drone.getSpeed());
            sb.append("\n");
            droneOutput.add(sb.toString());
        }
        return droneOutput;
    }

    private List<String> parseOrders() {
        List<String> orderOutput = new ArrayList<>();
        StringBuilder sb;
        for (Order order : orders) {
            sb = new StringBuilder();
            sb.append("ORDER:");
            sb.append(order.getUser().getName());
            sb.append(":");
            Map<Dish, Number> orderQuantities = order.getBasket();
            for (Map.Entry<Dish, Number> dishQuantity : orderQuantities.entrySet()) {
                Dish dish = dishQuantity.getKey();
                Integer amount = dishQuantity.getValue().intValue();
                sb.append(amount + " * " + dish.getName() + ",");
            }
            //Used to remove the excess comma at the end of the list of dishes and quantities.
            sb.setLength(sb.length() - 1);
            sb.append("\n");
            orderOutput.add(sb.toString());
        }
        return orderOutput;
    }

    private List<String> parseStock() {
        List<String> stockOutput = new ArrayList<>();
        StringBuilder sb;
        for (StockItem stock : stockManager.getStock()) {
            sb = new StringBuilder();
            sb.append("STOCK:");
            sb.append(stock.getStockedItem().getName());
            sb.append(":");
            sb.append(stock.getStock());
            sb.append("\n");
            stockOutput.add(sb.toString());
        }
        return stockOutput;
    }


}
