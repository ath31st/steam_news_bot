create table if not exists users
(
    user_id  varchar(255) not null primary key,
    active   boolean      not null,
    locale   varchar(255),
    name     varchar(255),
    steam_id bigint
);
