
    create table AbstractTask (
       id bigint not null,
        comments varchar(500),
        endDate date,
        name varchar(50) not null,
        startDate date,
        primary key (id)
    ) engine=InnoDB;

    create table DefaultTask (
       id bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table hibernate_sequence (
       next_val bigint
    ) engine=InnoDB;

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    create table Imputation (
       id bigint not null,
        day date,
        value double precision,
        task_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Project (
       id bigint not null,
        attributes json,
        comments varchar(500),
        name varchar(50),
        quotation double precision,
        startDate date,
        primary key (id)
    ) engine=InnoDB;

    create table ProjectMembership (
       membershipID bigint not null,
        role varchar(255),
        member_id bigint,
        project_id bigint,
        primary key (membershipID)
    ) engine=InnoDB;

    create table Task (
       estimateWork double precision not null,
        origin varchar(255),
        remoteId bigint,
        remotePath varchar(255),
        id bigint not null,
        latestRevision_id bigint,
        project_id bigint,
        taskType_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table TaskRevision (
       id bigint not null,
        remainsToBeDone double precision not null,
        revisionDate datetime(6),
        assigned_id bigint,
        revisionActor_id bigint,
        task_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table TaskType (
       id bigint not null,
        typeName varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table User (
       id bigint not null,
        accountCreationTime date not null,
        beginWorkDate date not null,
        email varchar(255) not null,
        firstName varchar(255) not null,
        imputationFutur bit not null,
        login varchar(50) not null,
        matriculeID varchar(255),
        name varchar(255) not null,
        password varchar(255) not null,
        validateOwnImputation bit not null,
        primary key (id)
    ) engine=InnoDB;

    create table ValidatedTimesheet (
       id bigint not null,
        week integer,
        year integer,
        user_id bigint,
        validatedBy_id bigint,
        primary key (id)
    ) engine=InnoDB;

    alter table Imputation 
       add constraint UKsc0a68hjsx40d6xt9yep80o7l unique (day, task_id);

    alter table User 
       add constraint UK_587tdsv8u5cvheyo9i261xhry unique (login);

    alter table DefaultTask 
       add constraint FKmjpua8f0woa8mb5uaojwwxiba 
       foreign key (id) 
       references AbstractTask (id);

    alter table Imputation 
       add constraint FK77e4o89dpt066rpt2jpnbe0ba 
       foreign key (task_id) 
       references Task (id);

    alter table ProjectMembership 
       add constraint FKh59cv9s56u3sdi0ki6axsxf09 
       foreign key (member_id) 
       references User (id);

    alter table ProjectMembership 
       add constraint FKapg94jqua2lbkjdb0kofxtnln 
       foreign key (project_id) 
       references Project (id);

    alter table Task 
       add constraint FKjwuo5mqkfx9k23jd3g8vr4a2p 
       foreign key (latestRevision_id) 
       references TaskRevision (id);

    alter table Task 
       add constraint FKkkcat6aybe3nbvhc54unstxm6 
       foreign key (project_id) 
       references Project (id);

    alter table Task 
       add constraint FKigksw4egslpbdevlab7ucu8lb 
       foreign key (taskType_id) 
       references TaskType (id);

    alter table Task 
       add constraint FKrvdfql6piqe3nxp7ta02s1xm9 
       foreign key (id) 
       references AbstractTask (id);

    alter table TaskRevision 
       add constraint FKp9ssbxu7c3w7fr3jukkget1ne 
       foreign key (assigned_id) 
       references User (id);

    alter table TaskRevision 
       add constraint FK16welq7uyu2n2xmycgw23ebgq 
       foreign key (revisionActor_id) 
       references User (id);

    alter table TaskRevision 
       add constraint FKpsj9t1js8flo735q3nx3o0c6d 
       foreign key (task_id) 
       references Task (id);

    alter table ValidatedTimesheet 
       add constraint FKf4lmab2846nt5smlforv45yj3 
       foreign key (user_id) 
       references User (id);

    alter table ValidatedTimesheet 
       add constraint FK7ffrinnv0q59rfi7a1dkjvh26 
       foreign key (validatedBy_id) 
       references User (id);
