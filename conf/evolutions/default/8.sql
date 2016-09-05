# Users schema

# --- !Ups

CREATE TABLE chatbotmessages (
    id BIGSERIAL NOT NULL,
    message text NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE chatbotmessages;
