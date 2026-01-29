package com.restaurant.models;

import java.time.Instant;
import java.util.List;

public class Table {
    private int id;
    private int number;
    private List<Order> orders;

    public Table() {}

    public Table(int id, int number, List<Order> orders) {
        this.id = id;
        this.number = number;
        this.orders = orders;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", number=" + number +
                ", orders=" + orders +
                '}';
    }

    public Boolean isAvailableAt(Instant t) {
        if (orders == null || orders.isEmpty()) {
            return true;
        }
        for (Order order : orders) {
            TableOrder tableOrder = order.getTable();
            if (tableOrder == null) {
                continue;
            }
            Instant arrival = tableOrder.getArrivalDatetime();
            Instant departure = tableOrder.getDepartureDatetime();
            if (arrival != null) {
                if (departure == null) {
                    Instant estimatedDeparture = arrival.plusSeconds(2 * 60 * 60);
                    if (!t.isBefore(arrival) && t.isBefore(estimatedDeparture)) {
                        return false;
                    }
                }
                else {
                    if (!t.isBefore(arrival) && t.isBefore(departure)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
