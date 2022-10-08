create table User (
  id bigint generated by default as identity (start with 1),
  email varchar(255),
  fullname varchar(255),
  is_admin bit not null,
  password varchar(255),
  
  primary key (id)
);

create table Post (
  id bigint generated by default as identity (start with 1),
  content longvarchar,
  posted_at timestamp,
  title varchar(255),
  author_id bigint,

  primary key (id)
);

create table Tag (
  id bigint generated by default as identity (start with 1),
  name varchar(255),

  primary key (id)
);

create table Post_Tag (
  post_id bigint not null,
  tag_id bigint not null,

 primary key (Post_id, tag_id)
);

create table Comment (
  id bigint generated by default as identity (start with 1),
  author varchar(255),
  content longvarchar, 
  posted_at timestamp, 
  post_id bigint, 
  
  primary key (id)
);


alter table Post add constraint Post_FK1 foreign key (author_id) references User;
alter table Post_Tag add constraint Post_Tag_FK1 foreign key (tag_id) references Tag;
alter table Post_Tag add constraint Post_Tag_FK2 foreign key (Post_id) references Post on delete cascade;
alter table Comment add constraint Comment_FK1 foreign key (post_id) references Post(id) on delete cascade;
