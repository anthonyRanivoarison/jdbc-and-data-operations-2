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

    private final DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        var connection = dbConnection.getDBConnection();
        try {
            String dishQuery = "SELECT d.id as dishId, d.name as dishName, d.dish_type, d.price as dishPrice, " +
                    "i.id as ingredientId, i.name as ingredientName, i.price as ingredientPrice, " +
                    "i.category, i.id_dish " +
                    "FROM dish d " +
                    "LEFT JOIN ingredient i ON d.id = i.id_dish " +
                    "WHERE d.id = ?";

            PreparedStatement dishPs = connection.prepareStatement(dishQuery);
            dishPs.setInt(1, id);
            ResultSet dishResultSet = dishPs.executeQuery();

            Dish dish = null;
            List<Ingredient> ingredients = new ArrayList<>();

            while (dishResultSet.next()) {
                if (dish == null) {
                    dish = new Dish();
                    dish.setId(dishResultSet.getInt("dishId"));
                    dish.setName(dishResultSet.getString("dishName"));
                    dish.setDishType(DishTypeEnum.valueOf(dishResultSet.getString("dish_type")));
                    dish.setPrice(dishResultSet.getDouble("dishPrice"));
                }

                Ingredient ingredient = new Ingredient();
                ingredient.setId(dishResultSet.getInt("ingredientId"));
                ingredient.setName(dishResultSet.getString("ingredientName"));
                ingredient.setPrice(dishResultSet.getDouble("ingredientPrice"));
                ingredient.setCategory(CategoryEnum.valueOf(dishResultSet.getString("category")));

                ingredients.add(ingredient);
            }

            if (dish != null) {
                dish.setIngredients(ingredients);
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

            String query = "SELECT i.id, i.name, i.price, i.category, d.id, d.name, d.dish_type, d.price " +
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
                Double dishPrice = resultSet.getDouble(8);
                Dish ingredientDish = new Dish(dishId, dishName, dishType, null, dishPrice);

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
            String isAlreadyInDBQuery = "SELECT id, name FROM dish WHERE name = ?";
            PreparedStatement isAlreadyInDBPreparedStmt = connection.prepareStatement(isAlreadyInDBQuery);
            isAlreadyInDBPreparedStmt.setString(1, dishToSave.getName());
            ResultSet resultSet = isAlreadyInDBPreparedStmt.executeQuery();

            if (resultSet.next()) {
                int existingDishId = resultSet.getInt("id");

                String updateDishQuery = "UPDATE dish SET name = ?, dish_type = ?, price = ? WHERE id = ?";
                PreparedStatement updateDishPreparedStmt = connection.prepareStatement(updateDishQuery);
                updateDishPreparedStmt.setString(1, dishToSave.getName());
                updateDishPreparedStmt.setObject(2, dishToSave.getDishType().name(), Types.OTHER);
                updateDishPreparedStmt.setDouble(3, dishToSave.getPrice());
                updateDishPreparedStmt.setInt(4, existingDishId);
                updateDishPreparedStmt.executeUpdate();

                String deleteIngredientsQuery = "DELETE FROM ingredient WHERE id_dish = ?";
                PreparedStatement deleteIngredientsPreparedStmt = connection.prepareStatement(deleteIngredientsQuery);
                deleteIngredientsPreparedStmt.setInt(1, existingDishId);
                deleteIngredientsPreparedStmt.executeUpdate();

                String insertIngredientQuery = "INSERT INTO ingredient(name, price, category, id_dish) VALUES (?, ?, ?, ?)";
                PreparedStatement insertIngredientPreparedStmt = connection.prepareStatement(insertIngredientQuery);
                for (Ingredient ingredient : dishToSave.getIngredients()) {
                    insertIngredientPreparedStmt.setString(1, ingredient.getName());
                    insertIngredientPreparedStmt.setDouble(2, ingredient.getPrice());
                    insertIngredientPreparedStmt.setObject(3, ingredient.getCategory().name(), Types.OTHER);
                    insertIngredientPreparedStmt.setInt(4, existingDishId);
                    insertIngredientPreparedStmt.executeUpdate();
                }

                return "Dish '" + dishToSave.getName() + "' mis à jour";

            } else {
                String insertDishQuery = "INSERT INTO dish(name, dish_type, price) VALUES (?, ?, ?) RETURNING id";
                PreparedStatement insertDishPreparedStmt = connection.prepareStatement(insertDishQuery);
                insertDishPreparedStmt.setString(1, dishToSave.getName());
                insertDishPreparedStmt.setObject(2, dishToSave.getDishType().name(), Types.OTHER);
                insertDishPreparedStmt.setDouble(3, dishToSave.getPrice());

                ResultSet rs = insertDishPreparedStmt.executeQuery();
                int newDishId = 0;
                if (rs.next()) {
                    newDishId = rs.getInt("id");
                }

                String insertIngredientQuery = "INSERT INTO ingredient(name, price, category, id_dish) VALUES (?, ?, ?, ?)";
                PreparedStatement insertIngredientPreparedStmt = connection.prepareStatement(insertIngredientQuery);
                for (Ingredient ingredient : dishToSave.getIngredients()) {
                    insertIngredientPreparedStmt.setString(1, ingredient.getName());
                    insertIngredientPreparedStmt.setDouble(2, ingredient.getPrice());
                    insertIngredientPreparedStmt.setObject(3, ingredient.getCategory().name(), Types.OTHER);
                    insertIngredientPreparedStmt.setInt(4, newDishId);
                    insertIngredientPreparedStmt.executeUpdate();
                }

                return "Dish '" + dishToSave.getName() + "' créé avec succès";
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
