package com.restaurant.models;

import java.time.Instant;

public class TableOrder {
    private RestaurantTable table;
    private Instant arrivalDatetime;
    private Instant departureDatetime;

    public TableOrder() {}

    public TableOrder(Instant arrivalDatetime, Instant departureDatetime, RestaurantTable table) {
        this.arrivalDatetime = arrivalDatetime;
        this.departureDatetime = departureDatetime;
        this.table = table;
    }

    public Instant getArrivalDatetime() {
        return arrivalDatetime;
    }

    public void setArrivalDatetime(Instant arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public Instant getDepartureDatetime() {
        return departureDatetime;
    }

    public void setDepartureDatetime(Instant departureDatetime) {
        this.departureDatetime = departureDatetime;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public void setTable(RestaurantTable table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "TableOrder{" +
                "arrivalDatetime=" + arrivalDatetime +
                ", table=" + table +
                ", departureDatetime=" + departureDatetime +
                '}';
    }
}
