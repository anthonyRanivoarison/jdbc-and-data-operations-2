package com.restaurant.test;

import com.restaurant.repository.DataRetriever;

public class DataRetrieverTest {

    public static void main(String[] args) {
        var dataRetriever = new DataRetriever();

        System.out.println("Dish with ID 1:");
        System.out.println(dataRetriever.findDishById(1));
        System.out.println("Dish with ID 999");
        System.out.println(dataRetriever.findDishById(999));

        System.out.println("Ingredients on page 2 and size 2:");
        System.out.println(dataRetriever.findIngredients(2, 2));

        System.out.println("Ingredients on page 3 and size 5:");
        System.out.println(dataRetriever.findIngredients(3, 5));

    }

}
