--
-- This script contains the ddl to create the engine database.
--
--
-- @version $Id: create_engine.ddl,v 1.1 2006-08-07 21:31:59 nichele Exp $
--

create table fnbl_principal
(
  username    varchar2(32 ) not null,
  device      varchar2(128) not null,
  id          varchar2(128) not null,
  primary key (id)
);

create table fnbl_user
(
  username   varchar2(32 ) not null,
  password   varchar2(32 ),
  email      varchar2(50 ),
  first_name varchar2(255),
  last_name  varchar2(255),
  internal_user char(1) default 'N',
  primary key (username)
);

create table fnbl_device
(
  id              varchar2(128) not null,
  description     varchar2(255),
  type            varchar2(255),
  digest          varchar2(255),
  client_nonce    varchar2(255),
  server_nonce    varchar2(255),
  server_password varchar2(255),
  constraint pk_fnbl_device primary key (id)
);

create table fnbl_id
(
  idspace varchar2(30) not null,
  counter int          not null,
  constraint pk_fnbl_id primary key (idspace)
);

create table fnbl_role
(
  role        varchar2(128) not null,
  description varchar2(200) not null,
  constraint pk_fnbl_role primary key (role)
);

create table fnbl_user_role
(
  username varchar2(32 ) not null,
  role     varchar2(128) not null,
  constraint pk_fnbl_user_role primary key (username,role)
);

create table fnbl_dm_state
(
    id        varchar2(32)  ,
    device    varchar2(128) ,
    mssid     varchar2(32)  ,
    state     char(1)       ,
    start_ts  date          ,
    end_ts    date          ,
    operation varchar2(255 ),
    info      varchar2(1024),
    constraint pk_fnbl_dm_state primary key (id)
);

create index nxd_dm_state_dev   on fnbl_dm_state (device   );
create index nxd_dm_state_sid   on fnbl_dm_state (mssid    );
create index nxd_dm_state_state on fnbl_dm_state (state    );
create index nxd_dm_state_op    on fnbl_dm_state (operation);
create index nxd_dm_state_info  on fnbl_dm_state (info     );

--
-- CONSTRAINTS
--
alter table fnbl_principal add constraint fk_user foreign key (
  username
)
references fnbl_user (
  username
);

alter table fnbl_principal add constraint fk_device foreign key (
  device
)
references fnbl_device (
  id
);

alter table fnbl_dm_state add constraint fk_device1 foreign key (
  device
)
references fnbl_device (
  id
);
