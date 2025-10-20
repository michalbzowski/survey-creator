;--[Hibernate]
    set client_min_messages = WARNING
;--[Hibernate]
    alter table if exists answers
       drop constraint if exists FKkw9v0ofgb1ct8g0u3ot00fknq
;--[Hibernate]
    alter table if exists answers
       drop constraint if exists FKrwcax3668umuvoopknh436pi5
;--[Hibernate]
    alter table if exists answers
       drop constraint if exists FK64k8uhl7hl53ujygbqdkqjcxn
;--[Hibernate]
    alter table if exists events
       drop constraint if exists FKpp326w6hkksd9r1n696qa7pfv
;--[Hibernate]
    alter table if exists person_group
       drop constraint if exists FKb9l9rg2g7pqsr457prmhrcntk
;--[Hibernate]
    alter table if exists person_group
       drop constraint if exists FKiuq1ec2crebautk34jv5toaum
;--[Hibernate]
    alter table if exists persons
       drop constraint if exists FKeg2x5kl8cv8rglf8dpi5y7dcq
;--[Hibernate]
    alter table if exists team_event
       drop constraint if exists FKm4nc671p32vgtx4idl3sy1q6h
;--[Hibernate]
    alter table if exists team_event
       drop constraint if exists FKerrqsi3rchcilqnj9v4ngd1lu
;--[Hibernate]
    alter table if exists team_member
       drop constraint if exists FKijlvjelj7rsx6lq6gltu1pba3
;--[Hibernate]
    alter table if exists team_member
       drop constraint if exists FK53oqnmt6lsci5gh3g8yqw12i7
;--[Hibernate]
    drop table if exists answers cascade
;--[Hibernate]
    drop table if exists communication_agreement cascade
;--[Hibernate]
    drop table if exists communication_team_links cascade
;--[Hibernate]
    drop table if exists communications cascade
;--[Hibernate]
    drop table if exists events cascade
;--[Hibernate]
    drop table if exists groups cascade
;--[Hibernate]
    drop table if exists integrations cascade
;--[Hibernate]
    drop table if exists members cascade
;--[Hibernate]
    drop table if exists messenger_user_agreement cascade
;--[Hibernate]
    drop table if exists person_group cascade
;--[Hibernate]
    drop table if exists persons cascade
;--[Hibernate]
    drop table if exists tags cascade
;--[Hibernate]
    drop table if exists team_event cascade
;--[Hibernate]
    drop table if exists team_member cascade
;--[Hibernate]
    drop table if exists teams cascade
;--[Hibernate]
    create table answers (
        event_id uuid,
        id uuid not null,
        member_id uuid,
        team_id uuid,
        answerValue varchar(255) check (answerValue in ('TAK','NIE','ODPOWIEM_POZNIEJ')),
        primary key (id)
    )
;--[Hibernate]
    create table communication_agreement (
        agree boolean not null,
        id uuid not null,
        personId uuid not null,
        registered_user_id uuid not null,
        channel varchar(255) check (channel in ('EMAIL','MESSENGER','WHATS_UP','SMS')),
        personEmail varchar(255) not null,
        properties jsonb,
        primary key (id)
    )
;--[Hibernate]
    create table communication_team_links (
        communicationId uuid not null,
        id uuid not null,
        teamEntryId uuid not null,
        primary key (id)
    )
;--[Hibernate]
    create table communications (
        id uuid not null,
        personId uuid not null,
        channel varchar(255) check (channel in ('EMAIL','MESSENGER','WHATS_UP','SMS')),
        communicationTemplate varchar(255) check (communicationTemplate in ('EMAIL_NEW_PERSON_ADDED','TEAM_RECORD_LINK')),
        personEmail varchar(255) not null,
        personFirstName varchar(255) not null,
        personLastName varchar(255) not null,
        status varchar(255) check (status in ('TO_SEND','SENT','ERROR')),
        properties jsonb,
        primary key (id)
    )
;--[Hibernate]
    create table events (
        localDateTime timestamp(6) not null,
        id uuid not null,
        registered_user_id uuid not null,
        team_id uuid,
        description varchar(4096) not null,
        location varchar(255) not null,
        name varchar(255) not null,
        primary key (id)
    )
;--[Hibernate]
    create table groups (
        id uuid not null,
        registered_user_id uuid not null,
        name varchar(255) not null,
        primary key (id)
    )
;--[Hibernate]
    create table integrations (
        id uuid not null,
        registered_user_id uuid not null,
        configuration jsonb,
        primary key (id)
    )
;--[Hibernate]
    create table members (
        teamAnswered boolean not null,
        id uuid not null,
        linkToken uuid not null unique,
        personId uuid,
        teamId uuid not null,
        personEmail varchar(255),
        personFirstName varchar(255),
        personLastName varchar(255),
        personTag varchar(255),
        primary key (id)
    )
;--[Hibernate]
    create table messenger_user_agreement (
        agree boolean not null,
        id uuid not null,
        messengerRegistrationKey uuid not null,
        registered_user_id uuid not null,
        email varchar(255) not null,
        psid varchar(255) not null,
        primary key (id)
    )
;--[Hibernate]
    create table person_group (
        group_id uuid not null,
        person_id uuid not null,
        primary key (group_id, person_id)
    )
;--[Hibernate]
    create table persons (
        id uuid not null,
        registered_user_id uuid not null,
        tag_id uuid,
        email varchar(255) not null,
        firstName varchar(255) not null,
        lastName varchar(255) not null,
        primary key (id),
        unique (email, registered_user_id)
    )
;--[Hibernate]
    create table tags (
        id uuid not null,
        registered_user_id uuid not null,
        name varchar(255) not null,
        primary key (id)
    )
;--[Hibernate]
    create table team_event (
        event_id uuid not null unique,
        team_id uuid not null
    )
;--[Hibernate]
    create table team_member (
        member_id uuid not null,
        team_id uuid not null,
        primary key (team_id)
    )
;--[Hibernate]
    comment on column team_member.team_id is
        ''
;--[Hibernate]
    create table teams (
        id uuid not null,
        registered_user_id uuid not null,
        name varchar(255),
        primary key (id)
    )
;--[Hibernate]
    alter table if exists answers
       add constraint FKkw9v0ofgb1ct8g0u3ot00fknq
       foreign key (event_id)
       references events
;--[Hibernate]
    alter table if exists answers
       add constraint FKrwcax3668umuvoopknh436pi5
       foreign key (member_id)
       references members
;--[Hibernate]
    alter table if exists answers
       add constraint FK64k8uhl7hl53ujygbqdkqjcxn
       foreign key (team_id)
       references teams
;--[Hibernate]
    alter table if exists events
       add constraint FKpp326w6hkksd9r1n696qa7pfv
       foreign key (team_id)
       references teams
;--[Hibernate]
    alter table if exists person_group
       add constraint FKb9l9rg2g7pqsr457prmhrcntk
       foreign key (group_id)
       references groups
;--[Hibernate]
    alter table if exists person_group
       add constraint FKiuq1ec2crebautk34jv5toaum
       foreign key (person_id)
       references persons
;--[Hibernate]
    alter table if exists persons
       add constraint FKeg2x5kl8cv8rglf8dpi5y7dcq
       foreign key (tag_id)
       references tags
;--[Hibernate]
    alter table if exists team_event
       add constraint FKm4nc671p32vgtx4idl3sy1q6h
       foreign key (event_id)
       references events
;--[Hibernate]
    alter table if exists team_event
       add constraint FKerrqsi3rchcilqnj9v4ngd1lu
       foreign key (team_id)
       references teams
;--[Hibernate]
    alter table if exists team_member
       add constraint FKijlvjelj7rsx6lq6gltu1pba3
       foreign key (member_id)
       references teams
;--[Hibernate]
    alter table if exists team_member
       add constraint FK53oqnmt6lsci5gh3g8yqw12i7
       foreign key (team_id)
       references members