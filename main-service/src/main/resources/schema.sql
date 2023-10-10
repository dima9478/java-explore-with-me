CREATE TABLE IF NOT EXISTS categories
(
    id   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users
(
    id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  varchar NOT NULL,
    email varchar NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title  varchar NOT NULL,
    pinned boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS events
(
    id                 bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title              varchar     NOT NULL,
    description        text        NOT NULL,
    annotation         varchar     NOT NULL,
    date               timestamp   NOT NULL,
    category_id        bigint      NOT NULL REFERENCES categories (id),
    paid               boolean DEFAULT false,
    request_moderation boolean DEFAULT true,
    participant_limit  integer DEFAULT 0,
    created_time       timestamp   NOT NULL,
    published_time     timestamp,
    state              varchar(20) NOT NULL,
    author_id          bigint      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    lat                double precision,
    lon                double precision
);

CREATE TABLE IF NOT EXISTS participant_requests
(
    id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id     bigint      NOT NULL REFERENCES events (id),
    requestor_id bigint      NOT NULL REFERENCES users (id),
    status       varchar(20) NOT NULL,
    created timestamp NOT NULL,
    CONSTRAINT uq_request UNIQUE (event_id, requestor_id)
);

CREATE TABLE IF NOT EXISTS events_compilations
(
    compilation_id bigint REFERENCES compilations (id),
    event_id       bigint REFERENCES events (id),
    PRIMARY KEY (compilation_id, event_id)
);