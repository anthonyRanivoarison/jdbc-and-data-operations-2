\c mini_dish_db;

truncate table stock_movement;

insert into stock_movement(id_ingredient, quantity, movement_type, unit_type) values
(1, 2, 'OUT', 'PCS'),
(2, 5, 'OUT', 'PCS'),
(3, 4, 'OUT', 'PCS'),
(4, 1, 'OUT', 'PCS'),
(5, 1, 'OUT', 'PCS');