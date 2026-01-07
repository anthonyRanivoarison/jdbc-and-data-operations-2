package com.restaurant.models;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double price;
    private List<Ingredient> ingredients;

    // Constructor
    public Dish() {

    }

    public Dish(int id, String name, DishTypeEnum dishType, List<Ingredient> ingredients, Double price) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
        this.price = price;
    }

    // Getter
    public DishTypeEnum getDishType() {
        return dishType;
    }

    public int getId() {
        return id;
    }

    public Double getPrice() {
        return price;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getName() {
        return name;
    }

    // Setter
    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // Equals and hash code
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(ingredients, dish.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, ingredients);
    }

    // toString
    @Override
    public String toString() {
        return "Dish{" +
                "dishType=" + dishType +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", price=" + getGrossMargin() +
                ", ingredients=" + ingredients +
                '}';
    }

    public Double getDishCost() {
        return ingredients.stream()
                .mapToDouble(Ingredient::getPrice)
                .sum();
    }

    public Double getGrossMargin() {
        if (price == null || price == 0) {
            throw new IllegalStateException("Pas de valeur pour le prix du plat");
        }
        return price - getDishCost();
    }
}
