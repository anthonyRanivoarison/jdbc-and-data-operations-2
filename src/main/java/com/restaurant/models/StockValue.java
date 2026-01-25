package com.restaurant.models;

import java.util.Objects;

public class StockValue {
    private Double quantity;
    private UnitEnum unit;

    public StockValue() {}

    public StockValue(Double quantity, UnitEnum unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StockValue that = (StockValue) o;
        return Objects.equals(quantity, that.quantity) && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, unit);
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public UnitEnum getUnit() {
        return unit;
    }

    public void setUnit(UnitEnum unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "StockValue{" +
                "quantity=" + quantity +
                ", unit=" + unit +
                '}';
    }
}
