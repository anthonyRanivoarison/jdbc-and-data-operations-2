package com.restaurant.models;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList;
    private Dish dish;

    public Ingredient() {}

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient(CategoryEnum category, Integer id, String name, Double price, List<StockMovement> stockMovementList) {
        this.category = category;
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockMovementList = stockMovementList;
    }

    public Ingredient(CategoryEnum category, Dish dish, Integer id, String name, Double price) {
        this.category = category;
        this.dish = dish;
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(price, that.price) && category == that.category && Objects.equals(stockMovementList, that.stockMovementList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, category, stockMovementList);
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "category=" + category +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockMovementList=" + stockMovementList +
                '}';
    }

    public StockValue getStockValueAt(Instant now) {
        var stockValue = new StockValue();
        for (StockMovement movement : stockMovementList) {
            if (movement.getCreationDatetime().isAfter(now)) {
                continue;
            }
            stockValue.setQuantity(movement.getStockValue().getQuantity());
            stockValue.setUnit(movement.getStockValue().getUnit());
        }
        return stockValue;
    }
}