package common;

import common.Model;
import common.UpdateListener;

import java.util.Collection;

public class Ingredient extends Model {

    private String measurementUnit;
    private Supplier supplier;
    private float restockThreshold;

    public Ingredient(String name, String unit, Supplier supplier) {
        super.setName(name);
        this.measurementUnit = unit;
        this.supplier = supplier;
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

    public void setMeasurementUnit(String measurementUnit) {
        notifyUpdate("unit",this.measurementUnit, measurementUnit);
        this.measurementUnit = measurementUnit;
    }

    public void setSupplier(Supplier newSupplier) {
        notifyUpdate("supplier",this.supplier, newSupplier);
        this.supplier = newSupplier;
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
