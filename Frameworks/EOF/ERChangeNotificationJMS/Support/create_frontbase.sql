drop table system_data;
create table system_data (
  id                     int not null,
  version                varchar(20) not null,
  creationDate           date not null
);
create unique index system_data_pk on system_data(id);

drop table message_id;
create table message_id (
  id                     int not null,
  maxId                  int not null
);
create unique index message_id_pk on message_id(id);

drop table seeds;
create table seeds (
  name                   varchar(20) not null,
  seed                   int not null
);
create unique index seeds_pk on seeds(name);

drop table destinations;
create table destinations (
  name                   varchar(255) not null,
  isQueue                boolean,
  destinationId          int not null
);
create unique index destinations_pk on destinations(destinationId);

drop table messages;
create table messages (
  messageId             longint,
  destinationId         longint not null,
  messageType           varchar(20) not null,
  priority              int,
  createTime            longint,
  expiryTime            longint,
  processed             int,
  messageBlob           blob not null
);
create index messages_pk on messages(messageId);

drop table message_handles;
create table message_handles (
   messageId            longint,
   destinationId        longint not null,
   consumerId           longint not null,
   priority             int,
   acceptedTime         longint,
   sequenceNumber       longint,
   expiryTime           longint,
   delivered            int
);
create index message_handles_pk on message_handles(destinationId, consumerId, messageId);

drop table consumers;
create table consumers (
  name                 varchar(50) not null,
  destinationId        longint not null,
  consumerId           longint not null,
  created              longint not null
);
create unique index consumers_pk on consumers(name, destinationId);

drop table jndi_context;
