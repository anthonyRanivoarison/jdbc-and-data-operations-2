package com.restaurant.models;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;

    public Order() {}

    public Order(Instant creationDatetime, List<DishOrder> dishOrders, Integer id, String reference) {
        this.creationDatetime = creationDatetime;
        this.dishOrders = dishOrders;
        this.id = id;
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) && Objects.equals(reference, order.reference) && Objects.equals(creationDatetime, order.creationDatetime) && Objects.equals(dishOrders, order.dishOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, creationDatetime, dishOrders);
    }

    @Override
    public String toString() {
        return "Order{" +
                "creationDatetime=" + creationDatetime +
                ", id=" + id +
                ", reference='" + reference + '\'' +
                ", dishOrders=" + dishOrders +
                '}';
    }

    public Double getTotalAmountWithoutVAT() {
        return dishOrders != null ? dishOrders.stream()
                .mapToDouble(dishOrder -> {
                    Dish dish = dishOrder.getDish();
                    if (dish == null || dish.getPrice() == null) {
                        return 0.0;
                    }
                    return dish.getPrice() * dishOrder.getQuantity();
                })
                .sum() : 0.0;
    }


    public Double getTotalAmountWithVAT() {
        double vatRate = 0.20;
        double totalWithoutVAT = getTotalAmountWithoutVAT();
        return totalWithoutVAT * (1 + vatRate);
    }
}
