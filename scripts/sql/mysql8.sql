
    create table Calendar (
       id bigint not null,
        name varchar(50),
        remoteId varchar(100),
        targetType varchar(25),
        primary key (id)
    ) engine=InnoDB;

    create table DataTableConfig (
       id bigint not null,
        columns tinyblob,
        tableInstanceId varchar(255),
        user_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table DefaultTask (
       id bigint not null,
        comments varchar(500),
        endDate date,
        name varchar(100) not null,
        origin varchar(255) not null,
        remoteId varchar(255),
        remotePath varchar(255),
        startDate date,
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

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    create table Imputation (
       id bigint not null,
        day date,
        value double precision,
        task_id bigint,
        user_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Milestone (
       id bigint not null,
        attributes TEXT,
        date date,
        name varchar(50),
        type integer,
        project_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Project (
       id bigint not null,
        attributes TEXT,
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
       id bigint not null,
        comments varchar(500),
        endDate date,
        name varchar(100) not null,
        origin varchar(255) not null,
        remoteId varchar(255),
        remotePath varchar(255),
        startDate date,
        effortLeft double precision not null,
        originalEstimate double precision not null,
        taskStatus integer not null,
        assigned_id bigint,
        milestone_id bigint,
        project_id bigint,
        taskType_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table TaskRevision (
       id bigint not null,
        effortLeft double precision not null,
        effortSpent double precision not null,
        originalEstimate double precision not null,
        realEffort double precision not null,
        revisionDate datetime(6),
        assigned_id bigint,
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
        externalIDs TEXT,
        firstName varchar(255),
        imputationFutur bit not null,
        name varchar(255),
        remoteSubject varchar(255),
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
       add constraint UK_ku4ibpw23c8xcgjt4sov3w3kv unique (remoteSubject);

    alter table DataTableConfig 
       add constraint FKor8rqcglt3u263qt792tdnpt9 
       foreign key (user_id) 
       references User (id);

    alter table Imputation 
       add constraint FKpv054mew449mf2m7itp50r57b 
       foreign key (user_id) 
       references User (id);

    alter table Milestone 
       add constraint FK4y2imlhl4and4511uh6lhnaiy 
       foreign key (project_id) 
       references Project (id);

    alter table ProjectMembership 
       add constraint FKh59cv9s56u3sdi0ki6axsxf09 
       foreign key (member_id) 
       references User (id);

    alter table ProjectMembership 
       add constraint FKapg94jqua2lbkjdb0kofxtnln 
       foreign key (project_id) 
       references Project (id);

    alter table Task 
       add constraint FKc44lafqphn0ecv9phdfate2kb 
       foreign key (assigned_id) 
       references User (id);

    alter table Task 
       add constraint FKjl7lj35hlsnb3n8x2kyk9w5lx 
       foreign key (milestone_id) 
       references Milestone (id);

    alter table Task 
       add constraint FKkkcat6aybe3nbvhc54unstxm6 
       foreign key (project_id) 
       references Project (id);

    alter table Task 
       add constraint FKigksw4egslpbdevlab7ucu8lb 
       foreign key (taskType_id) 
       references TaskType (id);

    alter table TaskRevision 
       add constraint FKp9ssbxu7c3w7fr3jukkget1ne 
       foreign key (assigned_id) 
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
