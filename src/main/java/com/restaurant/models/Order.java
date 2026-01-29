package com.restaurant.models;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrderList;
    private TableOrder table;

    public Order() {}

    public Order(Instant creationDatetime, List<DishOrder> dishOrderList, String reference, Integer id, TableOrder table) {
        this.creationDatetime = creationDatetime;
        this.dishOrderList = dishOrderList;
        this.id = id;
        this.reference = reference;
    }

    public Integer getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TableOrder getTable() {
        return table;
    }

    public void setTable(TableOrder table) {
        this.table = table;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrderList() {
        return dishOrderList;
    }

    public void setDishOrderList(List<DishOrder> dishOrderList) {
        this.dishOrderList = dishOrderList;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDatetime=" + creationDatetime +
                ", dishOrderList=" + dishOrderList +
                '}';
    }

    public Double getTotalAmountWithoutVAT() {
        return dishOrderList != null ? dishOrderList.stream()
            .mapToDouble(dishOrder -> {
                Dish dish = dishOrder.getDish();
                if (dish == null || dish.getPrice() == null) {
                 return 0.0;
                }
                return dish.getPrice() * dishOrder.getQuantity();}).sum() : 0.0;
    }


    public Double getTotalAmountWithVAT() {
        double vatRate = 0.20;
        double totalWithoutVAT = getTotalAmountWithoutVAT();
        return totalWithoutVAT * (1 + vatRate);
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Order order)) return false;
        return Objects.equals(id, order.id) && Objects.equals(reference, order.reference) && Objects.equals(creationDatetime, order.creationDatetime) && Objects.equals(dishOrderList, order.dishOrderList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, creationDatetime, dishOrderList);
    }
}