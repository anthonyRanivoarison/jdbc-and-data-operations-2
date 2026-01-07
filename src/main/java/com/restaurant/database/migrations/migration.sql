\c mini_dish_db;

alter table "dish"
    add column price numeric(10, 2) default 0.00;

update "dish" set price = 2000.00 where id = 1;
update "dish" set price = 6000.00 where id = 2;
update "dish" set price = null where id = 3;
update "dish" set price = null where id = 4;
update "dish" set price = null where id = 5;
