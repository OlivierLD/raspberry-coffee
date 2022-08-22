--
-- Migrating from HyperSonic SQL to SQLite
-- DB Schema here
--
drop table if exists tags;
drop table if exists images;
drop table if exists img_types;

create table img_types (
    name text primary key,
    description text
);

create table images (
    name text primary key,
    imagetype text not null,
    width integer not null,
    height integer not null,
    image BLOB not null,
    created date not null,
    foreign key(imagetype) references img_types(name)
);

create table tags (
    imgname text not null,
    rnk integer not null,
    label text not null,
    primary key (imgname, rnk),
    foreign key (imgname) references images(name) on delete cascade
);

insert into img_types values ('jpg',  'Joint Photographic Experts Group');
insert into img_types values ('png',  'Portable Network Graphics');
insert into img_types values ('gif',  'Graphics Interchange Format');
insert into img_types values ('tiff', 'Tagged Image File Format');
insert into img_types values ('bmp',  'Bitmap');
