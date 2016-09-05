# Users schema

# --- !Ups

CREATE TABLE "files" (
    fileid BIGSERIAL NOT NULL,
    userid  BIGSERIAL NOT NULL,
    file text NOT NULL,
    PRIMARY KEY (fileid),
    FOREIGN KEY (userid) REFERENCES "users" (id)
);
CREATE INDEX files_fileid_userid ON "files" (fileid, userid);

# --- !Downs

DROP INDEX files_fileid_userid;
DROP TABLE "files";
