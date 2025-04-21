create table if not exists user_game_state
(
    id        integer primary key,
    is_banned boolean not null,
    is_owned  boolean not null,
    is_wished boolean not null,
    game_id   varchar(255) references games,
    user_id   varchar(255) references users
);