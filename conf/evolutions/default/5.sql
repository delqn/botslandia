# Users schema

# --- !Ups

ALTER TABLE "files"
    ADD COLUMN filename varchar(255) NULL;

# --- !Downs

--
