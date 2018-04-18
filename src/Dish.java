import common.Model;
import common.UpdateListener;

import java.util.Collection;
import java.util.List;

public class Dish extends Model {

    private String dishDescription;
    //Price stored as int (multiplied by 100) to avoid floating point problems (eg: £3.50 != £3.500000001)
    private int intPrice;
    private List<IngredientAmount> ingredientAmounts;

    private Dish(String dishName, String dishDescription, float price, List<IngredientAmount> ingredients) {
        super.setName(dishName);
        this.dishDescription = dishDescription;
        //Prices can be given as floats, but they are multiplied by 100 to store them as integers. Eg: 1.50 becomes 150.
        this.intPrice = Math.round(price * 100);
        this.ingredientAmounts = ingredients;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    public void setPrice(float price) {
        notifyUpdate("price",(float)this.intPrice/100, price);
        this.intPrice = Math.round(price * 100);
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        super.addUpdateListener(listener);
    }

    @Override
    public void addUpdateListeners(Collection<UpdateListener> listeners) {
        super.addUpdateListeners(listeners);
    }

    @Override
    public void notifyUpdate() {
        super.notifyUpdate();
    }

    @Override
    public void notifyUpdate(String property, Object oldValue, Object newValue) {
        super.notifyUpdate(property, oldValue, newValue);
    }
}
