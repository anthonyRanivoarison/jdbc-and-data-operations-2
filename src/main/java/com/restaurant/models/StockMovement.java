package com.restaurant.models;

import java.time.Instant;
import java.util.Objects;

public class StockMovement {
    private Integer id;
    private StockValue stockValue;
    private MovementTypeEnum movementType;
    private Instant creationDatetime;

    public StockMovement() {}

    public StockMovement(Instant creationDatetime, Integer id, MovementTypeEnum movementType, StockValue stockValue) {
        this.creationDatetime = creationDatetime;
        this.id = id;
        this.movementType = movementType;
        this.stockValue = stockValue;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StockMovement that = (StockMovement) o;
        return Objects.equals(id, that.id) && Objects.equals(stockValue, that.stockValue) && movementType == that.movementType && Objects.equals(creationDatetime, that.creationDatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stockValue, movementType, creationDatetime);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MovementTypeEnum getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementTypeEnum movementType) {
        this.movementType = movementType;
    }

    public StockValue getStockValue() {
        return stockValue;
    }

    public void setStockValue(StockValue stockValue) {
        this.stockValue = stockValue;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "creationDatetime=" + creationDatetime +
                ", id=" + id +
                ", stockValue=" + stockValue +
                ", movementType=" + movementType +
                '}';
    }
}
