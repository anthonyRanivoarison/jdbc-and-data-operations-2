package com.restaurant.models;

public class RestaurantTable {
    private Integer id;
    private Integer tableNumber;

    public RestaurantTable() {}

    public RestaurantTable(Integer id, Integer tableNumber) {
        this.id = id;
        this.tableNumber = tableNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    @Override
    public String toString() {
        return "RestaurantTable{" +
                "id=" + id +
                ", tableNumber=" + tableNumber +
                '}';
    }
}
