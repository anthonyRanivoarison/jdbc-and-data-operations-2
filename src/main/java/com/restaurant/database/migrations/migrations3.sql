\c mini_dish_db;

create table "Order" (
    id serial primary key,
    reference varchar,
    creation_datetime timestamp default current_timestamp
);

create table DishOrder (
    id serial primary key,
    id_order int references "Order"(id),
    id_dish int references dish(id),
    quantity int
);