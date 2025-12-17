\c mini_dish_db;

insert into "dish"(id, name, dish_type) values
(1, 'Salaire fraiche', 'START'),
(2, 'Poulet grille', 'MAIN'),
(3, 'Riz au legumes', 'MAIN'),
(4, 'Gateau au chocolat', 'DESSERT'),
(5, 'Salade de fruits', 'DESSERT');

insert into "ingredient"(id, name, price, category, id_dish) values
(1, 'Laitue', 800.00, 'VEGETABLE', 1),
(2, 'Tomate', 600.00, 'VEGETABLE', 1),
(3, 'Poulet', 4500.00, 'ANIMAL', 2),
(4, 'Chocolat', 3000.00, 'OTHER', 4),
(5, 'Beurre', 2500.00, 'DAIRY', 4);

select setval(pg_get_serial_sequence('dish', 'id'),
        (select max(id) from "dish"));

select setval(pg_get_serial_sequence('ingredient', 'id'),
        (select max(id) from "ingredient"));
