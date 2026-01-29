package com.restaurant.repository;

import com.restaurant.config.DBConnection;
import com.restaurant.config.UnitConverter;
import com.restaurant.models.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;

public class DataRetriever {

    public Order findOrderByReference(String reference) {
        DBConnection dbConnection = new DBConnection();
        try (Connection connection = dbConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    select id, reference, creation_datetime from orders where reference like ?""");
            preparedStatement.setString(1, reference);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Order order = new Order();
                Integer idOrder = resultSet.getInt("id");
                order.setId(idOrder);
                order.setReference(resultSet.getString("reference"));
                order.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());
                order.setDishOrderList(findDishOrderByIdOrder(idOrder));
                return order;
            }
            throw new RuntimeException("Order not found with reference " + reference);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order findOrderById(Integer id) throws SQLException {
        String sql = """
        SELECT id, reference, creation_datetime, id_table, arrival_datetime, departure_datetime
        FROM orders
        WHERE id = ?
    """;
        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                    order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                    TableOrder tableOrder = new TableOrder();
                    RestaurantTable restaurantTable = findRestaurantTableById(rs.getInt("id_table"));
                    tableOrder.setTable(restaurantTable);
                    tableOrder.setArrivalDatetime(rs.getTimestamp("arrival_datetime").toInstant());

                    Timestamp departureTimestamp = rs.getTimestamp("departure_datetime");
                    if (departureTimestamp != null) {
                        tableOrder.setDepartureDatetime(departureTimestamp.toInstant());
                    }
                    order.setTable(tableOrder);
                    order.setDishOrderList(findDishOrderByIdOrder(id));

                    return order;
                }
                throw new RuntimeException("Order not found with id: " + id);
            }
        }
    }

    private List<RestaurantTable> getAvailableTablesAt(Connection conn, Instant arrivalDatetime, Instant departureDatetime) throws SQLException {
        String sql = "SELECT id, number FROM restaurant_table";
        List<RestaurantTable> availableTables = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Integer tableId = rs.getInt("id");
                if (isTableAvailable(conn, tableId, arrivalDatetime, departureDatetime)) {
                    RestaurantTable table = new RestaurantTable();
                    table.setId(tableId);
                    table.setTableNumber(rs.getInt("number"));
                    availableTables.add(table);
                }
            }
        }

        return availableTables;
    }

    private List<DishOrder> findDishOrderByIdOrder(Integer idOrder) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishOrder> dishOrders = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select id, id_dish, quantity from dishOrder where dishOrder.id_order = ?
                            """);
            preparedStatement.setInt(1, idOrder);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Dish dish = findDishById(resultSet.getInt("id_dish"));
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(resultSet.getInt("id"));
                dishOrder.setQuantity(resultSet.getInt("quantity"));
                dishOrder.setDish(dish);
                dishOrders.add(dishOrder);
            }
            dbConnection.closeConnection(connection);
            return dishOrders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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
                dish.setDishIngredients(findIngredientByDishId(id));
                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        String upsertIngredientSql = """
                    INSERT INTO ingredient (id, name, price, category)
                    VALUES (?, ?, ?, ?::dish_type)
                    ON CONFLICT (id) DO UPDATE
                    SET name = EXCLUDED.name,
                        category = EXCLUDED.category,
                        price = EXCLUDED.price
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer ingredientId;
            try (PreparedStatement ps = conn.prepareStatement(upsertIngredientSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getCategory().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    ingredientId = rs.getInt(1);
                }
            }

            insertIngredientStockMovements(conn, toSave);

            conn.commit();
            return findIngredientById(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertIngredientStockMovements(Connection conn, Ingredient ingredient) {
        List<StockMovement> stockMovementList = ingredient.getStockMovementList();
        String sql = """
                insert into stock_movement(id, id_ingredient, quantity, type, unit, creation_datetime)
                values (?, ?, ?, ?::movement_type, ?::unit, ?)
                on conflict (id) do nothing
                """;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            for (StockMovement stockMovement : stockMovementList) {
                if (ingredient.getId() != null) {
                    preparedStatement.setInt(1, ingredient.getId());
                } else {
                    preparedStatement.setInt(1, getNextSerialValue(conn, "stock_movement", "id"));
                }
                preparedStatement.setInt(2, ingredient.getId());
                preparedStatement.setDouble(3, stockMovement.getValue().getQuantity());
                preparedStatement.setObject(4, stockMovement.getType());
                preparedStatement.setObject(5, stockMovement.getValue().getUnit());
                preparedStatement.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        try (Connection connection = dbConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select id, name, price, category from ingredient where id = ?;");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int idIngredient = resultSet.getInt("id");
                String name = resultSet.getString("name");
                CategoryEnum category = CategoryEnum.valueOf(resultSet.getString("category"));
                Double price = resultSet.getDouble("price");
                return new Ingredient(category, idIngredient, name, price, findStockMovementsByIngredientId(idIngredient));
            }
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<StockMovement> findStockMovementsByIngredientId(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<StockMovement> stockMovementList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select id, quantity, unit_type, movement_type, creation_datetime
                            from stock_movement
                            where stock_movement.id_ingredient = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                StockMovement stockMovement = new StockMovement();
                stockMovement.setId(resultSet.getInt("id"));
                stockMovement.setType(MovementTypeEnum.valueOf(resultSet.getString("movement_type")));
                stockMovement.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());

                StockValue stockValue = new StockValue();
                stockValue.setQuantity(resultSet.getDouble("quantity"));
                stockValue.setUnit(UnitEnum.valueOf(resultSet.getString("unit_type")));
                stockMovement.setValue(stockValue);

                stockMovementList.add(stockMovement);
            }
            dbConnection.closeConnection(connection);
            return stockMovementList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

            List<DishIngredient> newDishIngredients = toSave.getDishIngredients();
            detachIngredients(conn, newDishIngredients);
            attachIngredients(conn, newDishIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ============================================
// Méthode saveDishOrder à ajouter dans DataRetriever
// ============================================

    private void saveDishOrder(Connection conn, List<DishOrder> dishOrders, Integer orderId) throws SQLException {
        String insertDishOrderSql = "INSERT INTO dishOrder (id, id_order, id_dish, quantity) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertDishOrderSql)) {
            for (DishOrder dishOrder : dishOrders) {
                int dishOrderId;
                if (dishOrder.getId() != null) {
                    dishOrderId = dishOrder.getId();
                } else {
                    dishOrderId = getNextSerialValue(conn, "dishOrder", "id");
                }
                ps.setInt(1, dishOrderId);
                ps.setInt(2, orderId);
                ps.setInt(3, dishOrder.getDish().getId());
                ps.setInt(4, dishOrder.getQuantity());
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private void updateIngredientStockMovement(Connection conn, Integer ingredientId, List<StockMovement> stockMovements) throws SQLException {
        if (stockMovements == null || stockMovements.isEmpty()) return;

        String insertSql = """
        INSERT INTO stock_movement
        (id, id_ingredient, quantity, unit_type, movement_type, creation_datetime)
        VALUES (?, ?, ?, ?::unit, ?::movement_type, ?)
    """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (StockMovement sm : stockMovements) {
                ps.setInt(1, sm.getId() != null ? sm.getId() : getNextSerialValue(conn, "stock_movement", "id"));
                ps.setInt(2, ingredientId);
                ps.setDouble(3, sm.getValue().getQuantity());
                ps.setString(4, sm.getValue().getUnit().name());
                ps.setString(5, sm.getType().name());
                ps.setTimestamp(6, Timestamp.from(sm.getCreationDatetime()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Order saveOrder(Order orderToSave) throws SQLException {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            connection.setAutoCommit(false);
            if (orderToSave.getTable() == null) {
                throw new RuntimeException("Table information is required for creating an order");
            }

            TableOrder tableOrder = orderToSave.getTable();

            if (tableOrder.getTable() == null || tableOrder.getTable().getId() == null) {
                throw new RuntimeException("Restaurant table is required");
            }
            if (tableOrder.getArrivalDatetime() == null) {
                throw new RuntimeException("Arrival datetime is required");
            }

            Integer requestedTableId = tableOrder.getTable().getId();
            Instant arrivalDatetime = tableOrder.getArrivalDatetime();
            Instant departureDatetime = tableOrder.getDepartureDatetime();

            RestaurantTable requestedTable;
            try {
                requestedTable = findRestaurantTableById(requestedTableId);
            } catch (RuntimeException e) {
                throw new RuntimeException("Table with ID " + requestedTableId + " does not exist");
            }

            boolean isRequestedTableAvailable = isTableAvailable(connection, requestedTableId, arrivalDatetime, departureDatetime);

            if (!isRequestedTableAvailable) {
                List<RestaurantTable> availableTables = getAvailableTablesAt(connection, arrivalDatetime, departureDatetime);

                String errorMessage;

                if (availableTables.isEmpty()) {
                    errorMessage = String.format(
                            "Table %d is not available at %s. No other tables are currently available.",
                            requestedTable.getTableNumber(),
                            arrivalDatetime
                    );
                } else {
                    String availableTableNumbers = availableTables.stream()
                            .map(t -> String.valueOf(t.getTableNumber()))
                            .collect(Collectors.joining(", "));

                    errorMessage = String.format(
                            "Table %d is not available at %s. Available tables: %s",
                            requestedTable.getTableNumber(),
                            arrivalDatetime,
                            availableTableNumbers
                    );
                }

                throw new RuntimeException(errorMessage);
            }

            for(DishOrder dishOrder: orderToSave.getDishOrderList()){
                for (DishIngredient dishIng: dishOrder.getDish().getDishIngredients()){
                    double requiredQuantity = dishIng.getQuantity() * dishOrder.getQuantity();
                    Ingredient ingredient = findIngredientById(dishIng.getIngredient().getId());

                    double availableStock = 0.0;
                    String stockQuery = """
                    SELECT COALESCE(
                        SUM(CASE 
                            WHEN movement_type = 'IN' THEN quantity 
                            WHEN movement_type = 'OUT' THEN -quantity 
                        END), 0) as current_stock
                    FROM stock_movement
                    WHERE id_ingredient = ?
                """;

                    try (PreparedStatement ps = connection.prepareStatement(stockQuery)) {
                        ps.setInt(1, ingredient.getId());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            availableStock = rs.getDouble("current_stock");
                        }
                    }

                    double requiredInKg;
                    switch (dishIng.getUnit()) {
                        case PCS -> requiredInKg = UnitConverter.piecesToKilogram(ingredient.getName(), requiredQuantity);
                        case L -> requiredInKg = UnitConverter.litresToKilogram(ingredient.getName(), requiredQuantity);
                        case KG -> requiredInKg = requiredQuantity;
                        default -> throw new IllegalStateException("Unsupported unit: " + dishIng.getUnit());
                    }

                    if(availableStock < requiredInKg){
                        throw new RuntimeException(
                                String.format("Insufficient stock for ingredient: %s. Required: %.2f KG, Available: %.2f KG",
                                        ingredient.getName(), requiredInKg, availableStock)
                        );
                    }
                }
            }

            String sql = """
            INSERT INTO orders (id, reference, creation_datetime, id_table, arrival_datetime, departure_datetime)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

            int orderId;
            if(orderToSave.getId() != null){
                orderId = orderToSave.getId();
            } else {
                orderId = getNextSerialValue(connection, "orders", "id");
            }

            try(PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                ps.setString(2, orderToSave.getReference());
                ps.setTimestamp(3, Timestamp.from(orderToSave.getCreationDatetime()));
                ps.setInt(4, requestedTableId);
                ps.setTimestamp(5, Timestamp.from(arrivalDatetime));
                ps.setTimestamp(6, departureDatetime != null ? Timestamp.from(departureDatetime) : null);

                ps.executeUpdate();
            }

            saveDishOrder(connection, orderToSave.getDishOrderList(), orderId);

            for(DishOrder dishOrder: orderToSave.getDishOrderList()){
                for (DishIngredient dishIng: dishOrder.getDish().getDishIngredients()){
                    double requiredQuantity = dishIng.getQuantity() * dishOrder.getQuantity();
                    Ingredient ingredient = findIngredientById(dishIng.getIngredient().getId());

                    double requiredInKg;
                    switch (dishIng.getUnit()) {
                        case PCS -> requiredInKg = UnitConverter.piecesToKilogram(ingredient.getName(), requiredQuantity);
                        case L -> requiredInKg = UnitConverter.litresToKilogram(ingredient.getName(), requiredQuantity);
                        case KG -> requiredInKg = requiredQuantity;
                        default -> throw new IllegalStateException("Unsupported unit");
                    }

                    StockMovement outMovement = new StockMovement();
                    outMovement.setId(getNextSerialValue(connection, "stock_movement", "id"));
                    StockValue outStockValue = new StockValue();
                    outStockValue.setQuantity(requiredInKg);
                    outStockValue.setUnit(UnitEnum.KG);
                    outMovement.setValue(outStockValue);
                    outMovement.setType(MovementTypeEnum.OUT);
                    outMovement.setCreationDatetime(Instant.now());

                    String insertStockSql = """
                    INSERT INTO stock_movement
                    (id, id_ingredient, quantity, unit_type, movement_type, creation_datetime)
                    VALUES (?, ?, ?, ?::unit, ?::movement_type, ?)
                """;

                    try (PreparedStatement ps = connection.prepareStatement(insertStockSql)) {
                        ps.setInt(1, outMovement.getId());
                        ps.setInt(2, ingredient.getId());
                        ps.setDouble(3, outStockValue.getQuantity());
                        ps.setString(4, outStockValue.getUnit().name());
                        ps.setString(5, outMovement.getType().name());
                        ps.setTimestamp(6, Timestamp.from(outMovement.getCreationDatetime()));
                        ps.executeUpdate();
                    }
                }
            }

            connection.commit();
            return findOrderById(orderId);

        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            dbConnection.closeConnection(connection);
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
                        INSERT INTO ingredient (id, name, category, price)
                        VALUES (?, ?, ?::ingredient_category, ?)
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

    private boolean isTableAvailable(Connection conn, Integer tableId, Instant seatedAt, Instant leftAt)
            throws SQLException {
        String checkAvailabilitySql = """
        SELECT COUNT(*) as conflict_count
        FROM orders
        WHERE id_table = ?
          AND (
              (? BETWEEN seated_at AND COALESCE(left_at, seated_at + INTERVAL '2 hours'))
              OR
              (? BETWEEN seated_at AND COALESCE(left_at, seated_at + INTERVAL '2 hours'))
              OR
              (? <= seated_at AND ? >= COALESCE(left_at, seated_at + INTERVAL '2 hours'))
          )
    """;

        try (PreparedStatement ps = conn.prepareStatement(checkAvailabilitySql)) {
            ps.setInt(1, tableId);
            ps.setTimestamp(2, Timestamp.from(seatedAt));
            ps.setTimestamp(3, leftAt != null ? Timestamp.from(leftAt) : null);
            ps.setTimestamp(4, Timestamp.from(seatedAt));
            ps.setTimestamp(5, leftAt != null ? Timestamp.from(leftAt) : null);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("conflict_count") == 0;
            }
        }
    }

    public RestaurantTable findRestaurantTableById(Integer id) {
        String sql = "SELECT id, number FROM restaurant_table WHERE id = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RestaurantTable table = new RestaurantTable();
                    table.setId(rs.getInt("id"));
                    table.setTableNumber(rs.getInt("number"));
                    return table;
                }
                throw new RuntimeException("Restaurant table not found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Order> findOrdersByTableId(Integer tableId) {
        String sql = """
            SELECT id, reference, creation_datetime, id_table, arrival_datetime, departure_datetime
            FROM orders
            WHERE id_table = ?
        """;

        List<Order> orders = new ArrayList<>();

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setReference(rs.getString("reference"));
                    order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                    // Créer le TableOrder
                    TableOrder tableOrder = new TableOrder();
                    RestaurantTable restaurantTable = findRestaurantTableById(rs.getInt("id_table"));
                    tableOrder.setTable(restaurantTable);
                    tableOrder.setArrivalDatetime(rs.getTimestamp("arrival_datetime").toInstant());

                    Timestamp departureTimestamp = rs.getTimestamp("departure_datetime");
                    if (departureTimestamp != null) {
                        tableOrder.setDepartureDatetime(departureTimestamp.toInstant());
                    }

                    order.setTable(tableOrder);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders by table id: " + e.getMessage(), e);
        }

        return orders;
    }

    public Table findTableById(Integer id) {
        try (Connection conn = new DBConnection().getConnection()) {
            String tableSql = "SELECT id, number FROM restaurant_table WHERE id = ?";
            Table table = new Table();
            try (PreparedStatement ps = conn.prepareStatement(tableSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        table.setId(rs.getInt("id"));
                        table.setNumber(rs.getInt("number"));
                    } else {
                        throw new RuntimeException("Table not found with id: " + id);
                    }
                }
            }

            table.setOrders(findOrdersByTableId(id));

            return table;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding table: " + e.getMessage(), e);
        }
    }

    public RestaurantTable saveRestaurantTable(RestaurantTable table) {
        String upsertTableSql = """
            INSERT INTO restaurant_table (id, number)
            VALUES (?, ?)
            ON CONFLICT (id) DO UPDATE
            SET number = EXCLUDED.number
            RETURNING id
        """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            Integer tableId;
            try (PreparedStatement ps = conn.prepareStatement(upsertTableSql)) {
                if (table.getId() != null) {
                    ps.setInt(1, table.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "restaurant_table", "id"));
                }
                ps.setInt(2, table.getTableNumber());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    tableId = rs.getInt(1);
                }
            }

            conn.commit();
            return findRestaurantTableById(tableId);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving restaurant table: " + e.getMessage(), e);
        }
    }

    private void detachIngredients(Connection conn, List<DishIngredient> dishIngredients) {
        Map<Integer, List<DishIngredient>> dishIngredientsGroupByDishId = dishIngredients.stream()
                .collect(Collectors.groupingBy(dishIngredient -> dishIngredient.getDish().getId()));
        dishIngredientsGroupByDishId.forEach((dishId, dishIngredientList) -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dish_ingredient where id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate(); // TODO: must be a grouped by batch
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void attachIngredients(Connection conn, List<DishIngredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        String attachSql = """
                    insert into dish_ingredient (id, id_ingredient, id_dish, required_quantity, unit)
                    values (?, ?, ?, ?, ?::unit)
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (DishIngredient dishIngredient : ingredients) {
                ps.setInt(1, getNextSerialValue(conn, "dish_ingredient", "id"));
                ps.setInt(2, dishIngredient.getIngredient().getId());
                ps.setInt(3, dishIngredient.getDish().getId());
                ps.setDouble(4, dishIngredient.getQuantity());
                ps.setObject(5, dishIngredient.getUnit());
                ps.addBatch(); // Can be substitute ps.executeUpdate() but bad performance
            }
            ps.executeBatch();
        }
    }

    private List<DishIngredient> findIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishIngredient> dishIngredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select ingredient.id, ingredient.name, ingredient.price, ingredient.category, ingredient.required_quantity, di.unit
                            from ingredient join "dishIngredient" di on di.id_ingredient = ingredient.id where ingredient.id_dish = ?;
                            """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

                DishIngredient dishIngredient = new DishIngredient();
                dishIngredient.setIngredient(ingredient);
                dishIngredient.setQuantity(resultSet.getObject("required_quantity") == null ? null : resultSet.getDouble("required_quantity"));
                dishIngredient.setUnit(UnitEnum.valueOf(resultSet.getString("unit")));

                dishIngredients.add(dishIngredient);
            }
            dbConnection.closeConnection(connection);
            return dishIngredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
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