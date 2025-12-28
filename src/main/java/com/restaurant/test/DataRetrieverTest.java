package com.restaurant.test;

import com.restaurant.models.CategoryEnum;
import com.restaurant.models.Dish;
import com.restaurant.models.DishTypeEnum;
import com.restaurant.models.Ingredient;
import com.restaurant.repository.DataRetriever;

import java.util.List;

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

        System.out.println("Create ingredient");
        var ingredients = List.of(
                new Ingredient(5, "Fromage", 1200.00, CategoryEnum.DAIRY),
                new Ingredient(6, "Oignon", 500.00, CategoryEnum.VEGETABLE)
        );
        System.out.println(dataRetriever.createIngredients(ingredients));

        System.out.println("Find dish by ingredient name");
        System.out.println(dataRetriever.findDishsByIngredientName("eur"));

        System.out.println("Save dish");
        var ingredient2 = new Ingredient(6, "Oignon", 5.00, CategoryEnum.VEGETABLE, new Dish());
        var dish = new Dish(5, "Soupe de légumes", DishTypeEnum.STARTER, List.of(ingredient2));
        System.out.println(dataRetriever.saveDish(dish));

        System.out.println("Find ingredients by criteria");
        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "gateau", 1, 1));

    }

}
