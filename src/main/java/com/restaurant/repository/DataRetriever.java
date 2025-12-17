package com.restaurant.repository;

import com.restaurant.config.DBConnection;
import com.restaurant.models.CategoryEnum;
import com.restaurant.models.Dish;
import com.restaurant.models.DishTypeEnum;
import com.restaurant.models.Ingredient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    public Dish findDishById(Integer id) {
        try {
            var connection = DBConnection.getDBConnection();

            String query = "SELECT d.id, d.name, d.dish_type, i.id, i.name, i.price, i.category FROM dish d JOIN ingredient i ON d.id = i.id_dish WHERE d.id = ?";
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();

            Dish dish = null;
            List<Ingredient> ingredients = new ArrayList<>();

            while (resultSet.next()) {
                int dishId = resultSet.getInt(1);
                String dishName = resultSet.getString(2);
                DishTypeEnum dishType = DishTypeEnum.valueOf(resultSet.getString(3));

                int ingredientId = resultSet.getInt(4);
                String ingredientName = resultSet.getString(5);
                double ingredientPrice = resultSet.getDouble(6);
                CategoryEnum ingredientCategoryType = CategoryEnum.valueOf(resultSet.getString(7));
                Dish ingredientDish = new Dish(dishId, dishName, dishType, null);

                Ingredient ingredient = new Ingredient(ingredientId, ingredientName, ingredientPrice, ingredientCategoryType, ingredientDish);

                ingredients.add(ingredient);

                dish = new Dish(dishId, dishName, dishType, ingredients);
            }

            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection();
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        try {
            var connection = DBConnection.getDBConnection();

            String query = "SELECT i.id, i.name, i.price, i.category, d.id, d.name, d.dish_type " +
                           "FROM ingredient i " +
                           "JOIN dish d ON i.id_dish = d.id " +
                           "LIMIT ? OFFSET ?";
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet resultSet = ps.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();

            while (resultSet.next()) {
                int ingredientId = resultSet.getInt(1);
                String ingredientName = resultSet.getString(2);
                double ingredientPrice = resultSet.getDouble(3);
                CategoryEnum ingredientCategoryType = CategoryEnum.valueOf(resultSet.getString(4));

                int dishId = resultSet.getInt(5);
                String dishName = resultSet.getString(6);
                DishTypeEnum dishType = DishTypeEnum.valueOf(resultSet.getString(7));
                Dish ingredientDish = new Dish(dishId, dishName, dishType, null);

                Ingredient ingredient = new Ingredient(ingredientId, ingredientName, ingredientPrice, ingredientCategoryType, ingredientDish);

                ingredients.add(ingredient);
            }

            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection();
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        try {
            var connection = DBConnection.getDBConnection();

            String query = "INSERT INTO ingredient (name, price, category, id_dish) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);

            for (Ingredient ingredient : newIngredients) {
                ps.setString(1, ingredient.getName());
                ps.setDouble(2, ingredient.getPrice());
                ps.setString(3, ingredient.getCategory().name());
                ps.setInt(4, ingredient.getDish().getId());
                ps.addBatch();
            }

            ps.executeBatch();

            return newIngredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection();
        }
    }

}
