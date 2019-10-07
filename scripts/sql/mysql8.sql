
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
        startDate date,
        primary key (id)
    ) engine=InnoDB;

    create table Project_ProjectCluster (
       projects_id bigint not null,
        clusters_id bigint not null,
        primary key (projects_id, clusters_id)
    ) engine=InnoDB;

    create table ProjectCluster (
       id bigint not null,
        name varchar(50),
        parent_id bigint,
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
        estimateWork double precision not null,
        name varchar(50) not null,
        remainsToBeDone double precision not null,
        startDate date,
        assigned_id bigint,
        project_id bigint,
        taskType_id bigint,
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

    alter table ProjectCluster 
       add constraint UK_3k2bn97rxcj148mqb4wo03cob unique (name);

    alter table User 
       add constraint UK_587tdsv8u5cvheyo9i261xhry unique (login);

    alter table Imputation 
       add constraint FK77e4o89dpt066rpt2jpnbe0ba 
       foreign key (task_id) 
       references Task (id);

    alter table Project_ProjectCluster 
       add constraint FK47ba3c71dora7chj7rtsp507u 
       foreign key (clusters_id) 
       references ProjectCluster (id);

    alter table Project_ProjectCluster 
       add constraint FK7coh4m9f6ytyd26sakeo74ju0 
       foreign key (projects_id) 
       references Project (id);

    alter table ProjectCluster 
       add constraint FKieve2cy647xh2xx4oq1j6pmsi 
       foreign key (parent_id) 
       references ProjectCluster (id);

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
       add constraint FKkkcat6aybe3nbvhc54unstxm6 
       foreign key (project_id) 
       references Project (id);

    alter table Task 
       add constraint FKigksw4egslpbdevlab7ucu8lb 
       foreign key (taskType_id) 
       references TaskType (id);

    alter table ValidatedTimesheet 
       add constraint FKf4lmab2846nt5smlforv45yj3 
       foreign key (user_id) 
       references User (id);

    alter table ValidatedTimesheet 
       add constraint FK7ffrinnv0q59rfi7a1dkjvh26 
       foreign key (validatedBy_id) 
       references User (id);
