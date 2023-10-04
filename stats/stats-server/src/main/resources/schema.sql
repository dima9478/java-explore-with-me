CREATE TABLE IF NOT EXISTS hits
(
    id   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app  varchar(100),
    uri  varchar(100),
    ip   varchar(25),
    date timestamp without time zone
);