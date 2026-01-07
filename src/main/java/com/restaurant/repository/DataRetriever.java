package com.restaurant.repository;

import com.restaurant.config.DBConnection;
import com.restaurant.models.CategoryEnum;
import com.restaurant.models.Dish;
import com.restaurant.models.DishTypeEnum;
import com.restaurant.models.Ingredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        var connection = dbConnection.getDBConnection();
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
            dbConnection.closeConnection(connection);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        var connection = dbConnection.getDBConnection();
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
            dbConnection.closeConnection(connection);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        var connection = dbConnection.getDBConnection();
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
            dbConnection.closeConnection(connection);
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        var connection = dbConnection.getDBConnection();
        try {
            String query = """
                    SELECT i.name, i.id_dish, d.id, d.name, d.dish_type FROM ingredient i
                    JOIN dish d ON d.id = i.id_dish WHERE i.name ILIKE ?
                    """;
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            List<Dish> dishes = new ArrayList<Dish>();
            if (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt(3));
                dish.setName(rs.getString(4));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString(5)));
                dishes.add(dish);
            }

            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    public String saveDish(Dish dishToSave) {
        var connection = dbConnection.getDBConnection();
        try {
            String isAlreadyInDBQuery = "SELECT name FROM dish WHERE name = ?";
            PreparedStatement isAlreadyInDBPreparedStmt = connection.prepareStatement(isAlreadyInDBQuery);
            isAlreadyInDBPreparedStmt.setString(1, dishToSave.getName());
            ResultSet resultSet = isAlreadyInDBPreparedStmt.executeQuery();
            if (resultSet.next()) {
                String updateDishQuery = "UPDATE dish SET id = ?, name = ?, dish_type = ?";
                PreparedStatement updateDishPreparedStmt = connection.prepareStatement(updateDishQuery);
                updateDishPreparedStmt.setInt(1, dishToSave.getId());
                updateDishPreparedStmt.setString(2, dishToSave.getName());
                updateDishPreparedStmt.setObject(3, dishToSave.getDishType(), Types.OTHER);

                String updateIngredientQuery = "UPDATE ingredient SET id = ?, name = ?, price = ?, category = ? WHERE id = ?";
                PreparedStatement updateIngredientPreparedStmt = connection.prepareStatement(updateIngredientQuery);
                for (Ingredient ingredient: dishToSave.getIngredients()) {
                    updateIngredientPreparedStmt.setInt(1, ingredient.getId());
                    updateDishPreparedStmt.setString(2, ingredient.getName());
                    updateIngredientPreparedStmt.setDouble(3, ingredient.getPrice());
                    updateDishPreparedStmt.setObject(4, ingredient.getCategory(), Types.OTHER);
                    updateIngredientPreparedStmt.setInt(5, dishToSave.getId());
                }
                int affectedRows = updateDishPreparedStmt.executeUpdate();
                return affectedRows > 0 ? "Dish avec les ingrédients mis à jour " + dishToSave.getIngredients().getFirst().getName() : "Aucun dish mis à jour";
            }
            String insertDishQuery = "INSERT INTO dish(id, name, dish_type, ingredients) VALUES (?, ?, ?, ?)";
            PreparedStatement insertDishPreparedStmt = connection.prepareStatement(insertDishQuery);
            insertDishPreparedStmt.setInt(1, dishToSave.getId());
            insertDishPreparedStmt.setString(2, dishToSave.getName());
            insertDishPreparedStmt.setString(3, String.valueOf(dishToSave.getDishType()));
            insertDishPreparedStmt.setObject(4, dishToSave.getIngredients());
            int affectedRows = insertDishPreparedStmt.executeUpdate();
            return affectedRows > 0 ? "Dish (" + dishToSave.getName() + ") créé avec succès contenant l’ingrédient Oignon" : "Aucun dish mis a jour";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        var connection = dbConnection.getDBConnection();
        try {
            var query = new StringBuilder("""
            SELECT i.id, i.name, i.price, i.category
            FROM ingredient i
            JOIN dish d ON i.id_dish = d.id
            WHERE 1=1
        """);

            if (ingredientName != null) query.append(" AND i.name ILIKE ?");
            if (category != null) query.append(" AND i.category = ?");
            if (dishName != null) query.append(" AND d.name ILIKE ?");
            query.append(" LIMIT ? OFFSET ?");

            PreparedStatement stmt = connection.prepareStatement(query.toString());
            int idx = 1;
            if (ingredientName != null) stmt.setString(idx++, "%" + ingredientName + "%");
            if (category != null) stmt.setString(idx++, category.name());
            if (dishName != null) stmt.setString(idx++, "%" + dishName + "%");
            stmt.setInt(idx++, size);
            stmt.setInt(idx++, (page - 1) * size);

            ResultSet rs = stmt.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredients.add(ingredient);
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

}
