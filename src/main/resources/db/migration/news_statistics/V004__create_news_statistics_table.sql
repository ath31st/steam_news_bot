create table news_statistics
(
    date        TEXT          not null primary key,
    daily_count INT default 0 not null,
    constraint chk_news_statistics_signed_integer_daily_count
        check (daily_count BETWEEN -2147483648 AND 2147483647)
);

create unique index main.news_statistics_date
    on news_statistics (date);

