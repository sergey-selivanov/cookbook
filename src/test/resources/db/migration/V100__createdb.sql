drop table if exists properties;
create table properties(
    property varchar(32) not null,
    val varchar(32) not null
);

create unique index uq_properties on properties(property);

drop table if exists recipes;
create table recipes(
    id identity,
    hash varchar(64) not null,    -- sha-256 digest
    title varchar(1024),
    packedfile blob,
    filesize bigint not null,
    dateadded bigint not null,
    originalfilename varchar(4096)
);

create unique index uq_recipes on recipes(hash);

drop table if exists tags;
create table tags(
    id identity,
    parentid bigint,    -- == identity == Long
    val varchar(128) not null,
    displayorder int not null,
    specialid int
);

create unique index uq_tags on tags(val);

drop table if exists recipetags;
create table recipetags(
    recipeid bigint not null,    -- == identity == Long
    tagid bigint not null
);

create unique index uq_recipetags on recipetags(recipeid, tagid);

insert into properties (property, val) values ('version', '1');

insert into tags (val, displayorder, specialid) values ('other', 99999, 1);
