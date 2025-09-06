;
    create table attendance_list (
        id uuid not null,
        registered_user_id uuid not null,
        name varchar(255),
        primary key (id)
    )
;
    create table attendance_list_events (
        attendance_list_id uuid not null,
        event_id uuid not null
    )
;
    create table events (
        localDateTime timestamp(6) not null,
        attendance_list_id uuid,
        id uuid not null,
        registered_user_id uuid not null,
        description varchar(4096) not null,
        location varchar(255) not null,
        name varchar(255) not null,
        primary key (id)
    )
;
    create table person_attendance_list_links (
        attendanceListAnswered boolean not null,
        attendanceListId uuid not null,
        attendanceList_id uuid not null,
        id uuid not null,
        linkToken uuid not null unique,
        personId uuid,
        personEmail varchar(255),
        personFirstName varchar(255),
        personLastName varchar(255),
        personTag varchar(255),
        status varchar(255) check (status in ('TO_SEND','SENT','ERROR')),
        primary key (id)
    )
;
    create table person_event_answers (
        attendance_list_id uuid,
        event_id uuid,
        id uuid not null,
        person_id uuid,
        answer varchar(255) check (answer in ('TAK','NIE','ODPOWIEM_POZNIEJ')),
        primary key (id)
    )
;
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
;
    create table tags (
        id uuid not null,
        registered_user_id uuid not null,
        name varchar(255) not null,
        primary key (id)
    )
;
    alter table if exists attendance_list_events 
       add constraint FKbry0dl3w7v0s1sno1cta89ayx 
       foreign key (event_id) 
       references events
;
    alter table if exists attendance_list_events 
       add constraint FKrl1u50fe4esthego4yyh7o3s9 
       foreign key (attendance_list_id) 
       references attendance_list
;
    alter table if exists events 
       add constraint FKf0ryb3223ycf4okssdqvhf0fj 
       foreign key (attendance_list_id) 
       references attendance_list
;
    alter table if exists person_attendance_list_links 
       add constraint FKm9j92i8hjjp7ckupxhcpew07 
       foreign key (attendanceList_id) 
       references attendance_list
;
    alter table if exists person_event_answers 
       add constraint FKnaptxf28iefqr1ugtwrffiv3u 
       foreign key (attendance_list_id) 
       references attendance_list
;
    alter table if exists person_event_answers 
       add constraint FKheawgvegh65gnjf3b6f69ivf4 
       foreign key (event_id) 
       references events
;
    alter table if exists person_event_answers 
       add constraint FK33187uqbl6w6tdgylufehkmk9 
       foreign key (person_id) 
       references persons
;
    alter table if exists persons 
       add constraint FKeg2x5kl8cv8rglf8dpi5y7dcq 
       foreign key (tag_id) 
       references tags