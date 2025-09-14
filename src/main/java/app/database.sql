create database tariffApp_db;

use tariffApp_db;

-- create table to store user login information
create table users (
    userid varchar(50) primary key,
    password varchar(255) not null,
);



