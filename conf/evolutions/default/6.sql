# Users schema

# --- !Ups

ALTER TABLE "users"
    ADD COLUMN google_user_id varchar(255) NULL UNIQUE;

CREATE INDEX users_google_user_id ON "users" (google_user_id);

# --- !Downs

DROP INDEX users_google_user_id;
ALTER TABLE "users" DROP COLUMN google_user_id;
