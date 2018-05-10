package common;

import java.io.Serializable;

public class Ingredient extends Model implements Serializable {

    private String measurementUnit;
    private Supplier supplier;
    private float restockThreshold = 0;

    public Ingredient(String name, String unit, Supplier supplier) {
        notifyUpdate("instantiation",null, this);
        super.setName(name);
        this.measurementUnit = unit;
        this.supplier = supplier;
    }

    public Ingredient(String name, String unit, Supplier supplier, float restockThreshold) {
        notifyUpdate("instantiation", null, this);
        super.setName(name);
        this.measurementUnit = unit;
        this.supplier = supplier;
        this.restockThreshold = restockThreshold;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        notifyUpdate("name", this.name, name);
        super.setName(name);
    }

    /**
     * Sets the unit for the ingredient
     * @param measurementUnit : String representation of the unit
     */
    public void setMeasurementUnit(String measurementUnit) {
        notifyUpdate("unit", this.measurementUnit, measurementUnit);
        this.measurementUnit = measurementUnit;
    }

    public void setRestockThreshold(float restockThreshold) {
        notifyUpdate("restock threshold", this.restockThreshold, restockThreshold);
        this.restockThreshold = restockThreshold;
    }

    public float getRestockThreshold() {
        return this.restockThreshold;
    }

    /**
     * Returns the supplier for this ingredient, used by the drone to find the distance to the supplier.
     * @return : Supplier of this Ingredient
     */
    public Supplier getSupplier() {
        return this.supplier;
    }

    /**
     * Sets the supplier for the ingredient
     * @param newSupplier : Supplier to change to
     */
    public void setSupplier(Supplier newSupplier) {
        notifyUpdate("supplier", this.supplier, newSupplier);
        this.supplier = newSupplier;
    }

    /**
     * A string representation of the data stored in the Ingredient
     * @return String : Representation of data stored in the Ingredient
     */
    @Override
    public String toString() {
        return this.name + " which is restocked when stock falls below " + restockThreshold + measurementUnit;
    }
}
