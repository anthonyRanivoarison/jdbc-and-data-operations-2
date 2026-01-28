package com.restaurant.test;

import com.restaurant.models.*;
import com.restaurant.repository.DataRetriever;

import java.time.Instant;
import java.util.List;

public class DataRetrieverTest {

    public static void main(String[] args) {
        var dataRetriever = new DataRetriever();

//        System.out.println("Dish with ID 1:");
//        System.out.println(dataRetriever.findDishById(1));

//        System.out.println("Dish with ID 999");
//        System.out.println(dataRetriever.findDishById(999));
//
//        var dish = dataRetriever.findDishById(5);
//        System.out.println(dish);
//        System.out.println("Dish cost: " + dish.getDishCost());
//        System.out.println("Dish gross margin: " + dish.getGrossMargin());

//        System.out.println("Ingredients on page 2 and size 2:");
//        System.out.println(dataRetriever.findIngredients(2, 2));

//        System.out.println("Ingredients on page 3 and size 5:");
//        System.out.println(dataRetriever.findIngredients(3, 5));

//        System.out.println("Create ingredient");
//        var ingredients = List.of(
//                new Ingredient(8, "Fromage", 1200.00, CategoryEnum.DAIRY, new Dish()),
//                new Ingredient(9, "Oignon", 500.00, CategoryEnum.VEGETABLE, new Dish())
//        );
//        System.out.println(dataRetriever.createIngredients(ingredients));

//        System.out.println("Find dish by ingredient name");
//        System.out.println(dataRetriever.findDishesByIngredientName("eur"));

//        System.out.println("Save dish");
//        var ingredient2 = new Ingredient(6, "Oignon", 3500.00, CategoryEnum.VEGETABLE, new Dish());
//        var dish2 = new Dish(5, "Soupe de légumes", DishTypeEnum.STARTER, List.of(ingredient2));
//        System.out.println(dataRetriever.saveDish(dish2));

//        System.out.println("Find ingredients by criteria");
//        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "sal", 1, 10));
//        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "gateau", 1, 10));

        System.out.println("Save order");
        List<DishOrder> dishOrders = List.of(
                new DishOrder(dataRetriever.findDishById(1), 1, 4),
                new DishOrder(dataRetriever.findDishById(2), 2, 2),
                new DishOrder(dataRetriever.findDishById(3), 3, 1)
        );
        var orderToSave = new Order(Instant.now(), dishOrders, 1, "ORD0001");
        var order = dataRetriever.saveOrder(orderToSave);
        System.out.println("Order: " + order);
        System.out.println("Get total order amount with VAT: " + order.getTotalAmountWithVAT());
        System.out.println("Get total order amount without VAT: " + order.getTotalAmountWithoutVAT());

//        System.out.println("Find order by ID");
//        System.out.println(dataRetriever.findIngredientStockMovements(1));

    }

}
