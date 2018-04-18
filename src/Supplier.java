import common.Model;
import common.UpdateListener;

import java.util.Collection;

public class Supplier extends Model {

    private float distance;

    public Supplier(String name, float distance) {
        super.setName(name);
        this.distance = distance;
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
