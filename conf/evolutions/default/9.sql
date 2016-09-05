# Users schema

# --- !Ups

CREATE TABLE kikbotmessages (
    id BIGSERIAL NOT NULL,
    message text NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE kikbotmessages;
