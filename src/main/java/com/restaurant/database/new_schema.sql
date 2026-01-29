\c mini_dish_db;

create type Unit_type as enum ('PCS', 'KG', 'L');

create table "dishIngredient" (
    id serial not null primary key,
    id_dish int references "dish"(id),
    id_ingredient int references "ingredient"(id),
    quantity_required numeric(10, 2),
    unit Unit_type
);

insert into "dishIngredient" (id, id_dish, id_ingredient, quantity_required, unit) values
(1, 1, 1, 0.20, 'KG'),
(2, 1, 2, 0.15, 'KG'),
(3, 2, 3, 1.00, 'KG'),
(4, 4, 4, 0.30, 'KG'),
(5, 4, 5, 0.20, 'KG');

update "dish" set price = 3500.00 where id = 1;
update "dish" set price = 12000.00 where id = 2;
update "dish" set price = null where id = 3;
update "dish" set price = 8000.00 where id = 4;
update "dish" set price = null where id = 5;

select * from orders;