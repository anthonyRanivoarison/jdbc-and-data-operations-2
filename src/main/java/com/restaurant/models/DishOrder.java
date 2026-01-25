package com.restaurant.models;

import java.util.Objects;

public class DishOrder {
    private Integer id;
    private Dish dish;
    private Integer quantity;

    public DishOrder() {}

    public DishOrder(Dish dish, Integer id, Integer quantity) {
        this.dish = dish;
        this.id = id;
        this.quantity = quantity;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DishOrder dishOrder = (DishOrder) o;
        return Objects.equals(id, dishOrder.id) && Objects.equals(dish, dishOrder.dish) && Objects.equals(quantity, dishOrder.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dish, quantity);
    }

    @Override
    public String toString() {
        return "DishOrder{" +
                "dish=" + dish +
                ", id=" + id +
                ", quantity=" + quantity +
                '}';
    }
}
