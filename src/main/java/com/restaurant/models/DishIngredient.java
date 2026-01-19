package com.restaurant.models;

public class DishIngredient {
    private Integer id;
    private Integer idDish;
    private Integer idIngredient;
    private Double quantity;
    private UnitEnum unit;

    public DishIngredient() {}

    public DishIngredient(Integer id, Integer idDish, Integer idIngredient, Double quantity, UnitEnum unit) {
        this.id = id;
        this.idDish = idDish;
        this.idIngredient = idIngredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getIdDish() { return idDish; }

    public void setIdDish(Integer idDish) { this.idDish = idDish; }

    public Integer getIdIngredient() { return idIngredient; }

    public void setIdIngredient(Integer idIngredient) { this.idIngredient = idIngredient; }

    public Double getQuantity() { return quantity; }

    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public UnitEnum getUnit() { return unit; }

    public void setUnit(UnitEnum unit) { this.unit = unit; }
}