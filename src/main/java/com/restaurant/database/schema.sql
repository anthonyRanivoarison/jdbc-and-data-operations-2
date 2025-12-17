\c mini_dish_db;

create type Ingredient_category as enum ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
create type Dish_type as enum ('START', 'MAIN', 'DESSERT');

create table "dish" (
    id serial not null primary key,
    name varchar(255) not null,
    dish_type Dish_type not null
);

create table "ingredient" (
    id serial not null primary key,
    name varchar(255) not null,
    price numeric(10,2) not null,
    category ingredient_category not null,
    id_dish int references "dish"(id)
);
