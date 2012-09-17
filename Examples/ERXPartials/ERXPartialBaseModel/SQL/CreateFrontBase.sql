connect to $1 user _system;
stop database;
delete database $1;
create database $1;
connect to $1 user _system;
create user $2;
set password $3 user $2;
create schema $2 authorization $2;
disconnect all;

