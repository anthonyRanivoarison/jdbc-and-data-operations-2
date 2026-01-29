package com.restaurant.test;

import com.restaurant.models.*;
import com.restaurant.repository.DataRetriever;

public class DataRetrieverTest {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        Dish saladeVerte = dataRetriever.findDishById(1);
        System.out.println("Salade verte: " + saladeVerte);

        Dish poulet = dataRetriever.findDishById(2);
        System.out.println("Poulet: " + poulet);

        Dish rizLegume = dataRetriever.findDishById(3);
        rizLegume.setPrice(100.0);
        Dish newRizLegume = dataRetriever.saveDish(rizLegume);
        System.out.println("Riz legume: " + newRizLegume);

        Ingredient laitue = dataRetriever.findIngredientById(1);
        System.out.println(laitue);

        System.out.println(dataRetriever.findOrderByReference("ORD0001"));

    }
}
