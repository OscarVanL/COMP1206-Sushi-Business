package common;

import exceptions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Oscar van Leusen
 */
public class Configuration {
    private Server server;
    private String fileName;

    public Configuration(Server server, String fileName) {
        this.fileName = fileName;
        this.server = server;
    }

    public void loadConfiguration() throws FileNotFoundException, InvalidSupplierException, InvalidStockItemException, InvalidIngredientException, InvalidPostcodeException, InvalidUserException, InvalidDishException {
        List<String> allLines;
        List<String> supplierLines;
        List<String> ingredientLines;
        List<String> dishLines;
        List<String> postcodeLines;
        List<String> userLines;
        List<String> orderLines;
        List<String> stockLines;
        List<String> staffLines;
        List<String> droneLines;
        try (Stream<String> fileStream = Files.lines(Paths.get(this.fileName))) {
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
            server.addSupplier(lineParse[1], Integer.parseInt(lineParse[2]));
        }
    }

    /**
     * Instantiates all Ingredients and their Stock data from the text representation of Ingredients from the Config file
     * @param ingredientLines : List containing non-parsed lines of Ingredient information.
     */
    private void loadIngredients(List<String> ingredientLines) throws InvalidSupplierException {
        //Adds all ingredients in the Configuration structure to our Suppliers array
        for (String line : ingredientLines) {
            //Structure: [0]INGREDIENT:[1]Name:[2]Unit:[3]Supplier:[4]Restock Threshold:[5]Restock Amount
            String[] lineParse = line.split(":");
            Supplier ingredientSupplier = null;
            //Finds supplier matching given name
            for (Supplier supplier : server.getSuppliers()) {
                if (supplier.getName().equals(lineParse[3])) {
                    ingredientSupplier = supplier;
                }
            }
            if (ingredientSupplier == null) {
                throw new InvalidSupplierException("Non-valid supplier entered for ingredient when reading Configuration file");
            }
            server.addIngredient(lineParse[1], lineParse[2], ingredientSupplier, Long.parseLong(lineParse[4]), Long.parseLong(lineParse[5]));
        }
    }

    /**
     * Instantiates all Dishes from the text representation of Dishes in the config file.
     * @param dishLines : List containing non-parsed lines of dish information.
     */
    private void loadDishes(List<String> dishLines) throws InvalidIngredientException {

        for (String line : dishLines) {
            //Structure: [0]DISH:[1]Name:[2]Description:[3]Price:[4]Restock Threshold:[5]Restock Amount:[6]Quantity * Item,Quantity * Item...
            String[] lineParse = line.split(":");
            //First creates the dish with no ingredients
            Dish newDish = server.addDish(lineParse[1], lineParse[2], Long.parseLong(lineParse[3]), Integer.parseInt(lineParse[4]), Integer.parseInt(lineParse[5]));

            //Parse each Ingredient (separated by a comma and a space). [0]ingredient 1, [1]ingredient 2, ...
            String[] ingredientParse = lineParse[6].split(",");

            //Iterates through each ingredient in the dish to add it.
            for (String ingredientData : ingredientParse) {
                //Parses Ingredients into structure: [0]Quantity,[1]Name
                String[] currentIngredient = ingredientData.split("\\s\\*\\s");
                //Looks for an existing ingredient matching the name of the ingredient to add
                Ingredient dishIngredient = null;
                for (Ingredient ingredient : server.getIngredients()) {
                    if (ingredient.getName().equals(currentIngredient[1])) {
                        dishIngredient = ingredient;
                    }
                }
                if (dishIngredient == null) {
                    throw new InvalidIngredientException("Non-valid ingredient: " + currentIngredient[1] + " entered for dish: " + lineParse[1] + " when reading Configuration file.");
                }
                server.addIngredientToDish(newDish, dishIngredient, Float.parseFloat(currentIngredient[0]));
            }
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
            server.addPostcode(lineParse[1], Long.parseLong(lineParse[2]));
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
            for (Postcode postcode : server.getPostcodes()) {
                if (postcode.getName().equals(lineParse[4])) {
                    userPostcode = postcode;
                    break;
                }
            }
            if (userPostcode == null) {
                throw new InvalidPostcodeException("Non-valid postcode entered for user: " + lineParse[1] + " when reading Configuration file.");
            }
            User user = new User(lineParse[1], lineParse[2], lineParse[3], userPostcode);
            server.addUser(user);
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
            Order order = null;
            for (User user : server.getUsers()) {
                if (user.getName().equals(lineParse[1])) {
                    orderUser = user;
                    order = new Order(user, user.getOrdersMade());
                    user.incrementOrdersMade();
                }
            }
            if (orderUser == null) {
                throw new InvalidUserException("Non-valid user: " + lineParse[1] + " entered for order when reading Configuration file.");
            }


            //Parse each ordered dish (separated by a comma), Structure: [0]Quantity * Dish, [1]Quantity * Dish, ...
            String[] orderContents = lineParse[2].split(",");
            for (String orderContent : orderContents) {
                //Structure: [0]Quantity, [1]Dish
                String[] orderInfo = orderContent.split("\\s\\*\\s");
                Dish orderDish = null;
                for (Dish dish : server.getDishes()) {
                    if (dish.getName().equals(orderInfo[1])) {
                        orderDish = dish;
                    }
                }
                if (orderDish == null) {
                    throw new InvalidDishException("Non-valid dish entered for " + orderUser.getName() + "'s order when reading configuration file.");
                }
                //Finally, adds the dish and quantity to the order.
                if (order != null) {
                    order.addDish(orderDish, Integer.parseInt(orderInfo[0]));
                    order.setOrderState(Order.OrderState.PREPARING);
                    server.notifyUpdate();
                }
            }
            server.addOrder(order);
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
            for (Dish dish : server.getDishes()) {
                if (dish.getName().equals(parseLine[1])) {
                    stockedItem = dish;
                }
            }
            for (Ingredient ingredient : server.getIngredients()) {
                if (ingredient.getName().equals(parseLine[1])) {
                    stockedItem = ingredient;
                }
            }
            if (stockedItem == null) {
                throw new InvalidStockItemException("Non-valid Dish or Ingredient entered for stock in Configuration file");
            }

            //Then finds whatever StockItem this dish/ingredient is contained in
            for (StockItem item : server.getStock()) {
                if (item.getStockedItem().equals(stockedItem)) {
                    item.setStock(Long.parseLong(parseLine[2]));
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
            server.addStaff(lineParse[1]);
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
            int speed = Integer.parseInt(lineParse[1]);
            if (speed > 0) {
                server.addDrone(speed);
            } else {
                System.out.println("Attempted to add invalid drone with speed 0");
            }
        }
    }

}
