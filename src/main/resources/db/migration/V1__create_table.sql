create table PARENT_MEMBER (
    uid BINARY(16) not null,
    name varchar(50),
    email varchar(100),
    primary key (uid)
);

create table SHOP (
    id bigint generated by default as identity,
    name varchar(50),
    address varchar(500),
    primary key (id)
);

create table SUBJECT (
    id bigint generated by default as identity,
    name varchar(50),
    primary key (id)
);

create table SHOP_CLASS (
    class_date date,
    class_time time(6),
    member_max integer,
    id bigint generated by default as identity,
    shop_id bigint,
    subject_id bigint,
    primary key (id)
);

create table ISLAND_BOOKING (
    created_at timestamp(6),
    id bigint generated by default as identity,
    shop_class_id bigint,
    parent_member_uid BINARY(16),
    primary key (id)
);