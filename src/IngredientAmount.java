public class IngredientAmount {

    private Ingredient ingredient;
    private float amount;

    public IngredientAmount(Ingredient ingredient, float amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    private Ingredient getIngredient() {
        return ingredient;
    }

    private float getAmount() {
        return amount;
    }
}
