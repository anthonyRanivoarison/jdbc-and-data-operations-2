package com.restaurant.repository;

import com.restaurant.config.DBConnection;
import com.restaurant.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    public Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select dish.id as dish_id, dish.name as dish_name, dish_type, dish.price as dish_price
                            from dish
                            where dish.id = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));
                dish.setIngredients(findIngredientByDishId(id));
                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateDishIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        String deleteSql = "DELETE FROM dishIngredient WHERE id_dish = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }

        if (ingredients != null && !ingredients.isEmpty()) {
            String insertSql = """
            INSERT INTO dishIngredient (id_dish, id_ingredient, quantity, unit)
            VALUES (?, ?, ?, ?)
        """;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : ingredients) {
                    ps.setInt(1, dishId);
                    ps.setInt(2, ingredient.getId());
                    ps.setDouble(3, ingredient.getQuantity());
                    ps.setString(4, String.valueOf(ingredient.getUnit()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
        INSERT INTO dish (id, price, name, dish_type)
        VALUES (?, ?, ?, ?::dish_type)
        ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name,
            dish_type = EXCLUDED.dish_type,
            price = EXCLUDED.price
        RETURNING id
    """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<Ingredient> newIngredients = toSave.getIngredients();
            updateDishIngredients(conn, dishId, newIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }
        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            String insertSql = """
                        INSERT INTO ingredient (id, name, category, price, required_quantity)
                        VALUES (?, ?, ?::ingredient_category, ?, ?)
                        RETURNING id
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());
                    if (ingredient.getQuantity() != null) {
                        ps.setDouble(5, ingredient.getQuantity());
                    }else {
                        ps.setNull(5, Types.DOUBLE);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        var DBConnection = new DBConnection();
        Connection connection = DBConnection.getConnection();
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
                Dish ingredientDish = new Dish(dishId, dishName, dishType, dishPrice);

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

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        var DBConnection = new DBConnection();
        var connection = DBConnection.getConnection();
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
            DBConnection.closeConnection(connection);
        }
    }

    private List<Ingredient> findIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select ingredient.id, ingredient.name, ingredient.price, ingredient.category, ingredient.required_quantity
                            from ingredient where id_dish = ?;
                            """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                Object requiredQuantity = resultSet.getObject("required_quantity");
                ingredient.setQuantity(requiredQuantity == null ? null : resultSet.getDouble("required_quantity"));
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        var DBConnection = new DBConnection();
        var connection = DBConnection.getConnection();
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
            DBConnection.closeConnection(connection);
        }
    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}