# Users schema

# --- !Ups

DROP INDEX files_fileid_userid;
DROP TABLE "files";
CREATE TABLE "files" (
    id BIGSERIAL NOT NULL,
    userid  BIGSERIAL NOT NULL,
    file text NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (userid) REFERENCES "users" (id)
);
CREATE INDEX files_id_userid ON "files" (id, userid);

# --- !Downs

DROP INDEX files_id_userid;
DROP TABLE "files";
