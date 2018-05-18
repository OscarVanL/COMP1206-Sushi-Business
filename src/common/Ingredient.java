package common;

import exceptions.InvalidStockItemException;

import java.io.Serializable;

/**
 * @author Oscar van Leusen
 */
public class Ingredient extends Model implements Serializable {

    private String measurementUnit;
    private Supplier supplier;
    private StockManager stockManager;

    public Ingredient(String name, String unit, Supplier supplier, StockManager stockManager) {
        notifyUpdate("instantiation",null, this);
        super.setName(name);
        this.measurementUnit = unit;
        this.supplier = supplier;
        this.stockManager = stockManager;
    }

    /**
     * Gets the name of the ingredient
     * @return String : name of ingredient
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the ingredient
     * @param name : New name of ingredient
     */
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
     * Gets the unit this ingredient is measured in
     * @return String representation of the unit this ingredient is measured in.
     */
    public String getUnit() {
        return this.measurementUnit;
    }

    /**
     * Gets the Restock Threshold of this dish (The point to which the stock must fall before it is restocked)
     * @return : Long of the restock threshold.
     */
    public Long getRestockThreshold() {
        try {
            return stockManager.getRestockThreshold(this);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the Restock Amount of this dish (The amount of the dish that is restocked when it is restocked)
     * @return : Long of the Restock Amount
     */
    public Long getRestockAmount() {
        try {
            return stockManager.getRestockAmount(this);
        } catch (InvalidStockItemException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A string representation of the data stored in the Ingredient
     * @return String : Representation of data stored in the Ingredient
     */
    @Override
    public String toString() {
        return this.name;
    }
}
