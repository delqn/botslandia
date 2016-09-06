# Users schema

# --- !Ups

CREATE TABLE tokens (
    id BIGSERIAL NOT NULL,
    userid BIGINT NOT NULL REFERENCES users(id),
    service TEXT NULL,
    token TEXT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE tokens;
