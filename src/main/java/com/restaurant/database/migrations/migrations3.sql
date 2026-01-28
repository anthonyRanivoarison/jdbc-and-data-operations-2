\c mini_dish_db;

create table orders (
    id serial primary key,
    reference varchar,
    creation_datetime timestamp default current_timestamp
);

create table dishOrder (
    id serial primary key,
    id_order int references orders(id),
    id_dish int references dish(id),
    quantity int
);