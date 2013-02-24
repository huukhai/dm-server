create table fnbl_treediscovery_processor (
    id        varchar(32 ) not null primary key,
    root_node varchar(255)
);

create table fnbl_user_dm_demo (
    device_id varchar(128) not null primary key,
    password  varchar(255),
    name      varchar(255),
    company   varchar(255),
    job       varchar(255),
    address   varchar(255),
    city      varchar(255),
    zip       varchar(255),
    state     varchar(255),
    country   varchar(255),
    email     varchar(255),
    phone     varchar(255)
);


