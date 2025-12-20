package com.restaurant.repository;

import com.restaurant.config.DBConnection;
import com.restaurant.models.CategoryEnum;
import com.restaurant.models.Dish;
import com.restaurant.models.DishTypeEnum;
import com.restaurant.models.Ingredient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    public Dish findDishById(Integer id) {
        var connection = DBConnection.getDBConnection();
        try {
            String dishQuery = "SELECT id, name, dish_type from dish WHERE id = ?";
            PreparedStatement dishPs = connection.prepareStatement(dishQuery);
            dishPs.setInt(1, id);
            ResultSet dishResultSet = dishPs.executeQuery();
            Dish dish = null;
            List<Ingredient> ingredients = new ArrayList<>();

            if (dishResultSet.next()) {
                dish.setId(dishResultSet.getInt("id"));
                dish.setName(dishResultSet.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(dishResultSet.getString("dish_type")));
            }

            String ingredientsQuery = "SELECT id, name, price, category from ingredients WHERE id = ?";
            PreparedStatement ingredientsPs = connection.prepareStatement(ingredientsQuery);
            ResultSet ingredientsResultSet = ingredientsPs.executeQuery();
            while (ingredientsResultSet.next()) {
                Ingredient ingredient = null;
                ingredient.setId(ingredientsResultSet.getInt("id"));
                ingredient.setName(ingredientsResultSet.getString("name"));
                ingredient.setPrice(ingredientsResultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(ingredientsResultSet.getString("category")));
                ingredients.add(ingredient);
            }

            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        var connection = DBConnection.getDBConnection();
        try {

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
            DBConnection.closeConnection(connection);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        var connection = DBConnection.getDBConnection();
        try {
            String findIngredientQuery = "SELECT name FROM ingredient WHERE name = ?";
            PreparedStatement findStmt = connection.prepareStatement(findIngredientQuery);
            for (Ingredient ingredient: newIngredients) {
                findStmt.setString(1, ingredient.getName());
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    throw new Exception("L'ingrédient " + ingredient.getName() + " est deja existant");
                }
            }

            String query = "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(query);

            for (Ingredient ingredient : newIngredients) {
                insertStmt.setString(1, ingredient.getName());
                insertStmt.setDouble(2, ingredient.getPrice());
                insertStmt.setObject(3, ingredient.getCategory().name(), Types.OTHER);
                insertStmt.addBatch();
            }

            int[] results = insertStmt.executeBatch();

            if (results.length != newIngredients.size()) {
                throw new Exception("Tous les ingrédients n'ont pas été créés");
            }
            return newIngredients;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection);
        }
    }

}
