# Users schema

# --- !Ups

ALTER TABLE "files"
    ADD COLUMN content_type varchar(255) NULL UNIQUE;

# --- !Downs

ALTER TABLE "files" DROP COLUMN content_type;
