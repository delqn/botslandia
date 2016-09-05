# Users schema

# --- !Ups

CREATE TABLE "users" (
    id BIGSERIAL NOT NULL,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    fullname varchar(255) NOT NULL,
    isAdmin boolean NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE "users";
