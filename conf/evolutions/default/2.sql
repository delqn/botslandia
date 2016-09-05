# Users schema

# --- !Ups

ALTER TABLE "users"
    ADD COLUMN gender varchar(255) NULL,
    ADD COLUMN locale varchar(255) NULL,
    ADD COLUMN picture varchar(255) NULL;

# --- !Downs

--
