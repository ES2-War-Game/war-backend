-- db/create-test-db.sql
CREATE DATABASE wargame_test;
\connect wargame_test;
CREATE USER wargame WITH PASSWORD 'wargame';
GRANT ALL PRIVILEGES ON DATABASE wargame_test TO wargame;