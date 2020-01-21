
    create table Account (
       id bigint not null,
        accountCreationTime date not null,
        email varchar(255) not null,
        externalIDs TEXT,
        firstName varchar(255),
        name varchar(255),
        remoteSubject varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table Batch (
       id bigint not null,
        organizationID bigint,
        attributes TEXT,
        date date,
        name varchar(50),
        type integer,
        project_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Calendar (
       id bigint not null,
        organizationID bigint,
        name varchar(50),
        remoteId varchar(100),
        targetType varchar(25),
        primary key (id)
    ) engine=InnoDB;

    create table CostByCategory (
       id bigint not null,
        organizationID bigint,
        costPerDay double precision not null,
        costPerHour double precision not null,
        endDate date,
        startDate date not null,
        account_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table DataTableConfig (
       id bigint not null,
        organizationID bigint,
        columns varchar(255),
        tableInstanceId varchar(255),
        user_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table DefaultTask (
       id bigint not null,
        organizationID bigint,
        comments varchar(500),
        endDate date,
        name varchar(100) not null,
        origin varchar(255) not null,
        remoteId varchar(255),
        remotePath varchar(255),
        startDate date,
        organization_id bigint,
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

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    create table Imputation (
       id bigint not null,
        organizationID bigint,
        day date,
        value double precision,
        account_id bigint,
        task_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Organization (
       id bigint not null,
        createdDate datetime(6),
        enabled bit,
        name varchar(255) not null,
        setup TEXT,
        primary key (id)
    ) engine=InnoDB;

    create table OrganizationMembership (
       id bigint not null,
        creationDate date,
        role varchar(255),
        member_id bigint,
        organization_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Project (
       id bigint not null,
        organizationID bigint,
        attributes TEXT,
        comments varchar(500),
        enable bit,
        name varchar(50),
        quotation double precision,
        startDate date,
        primary key (id)
    ) engine=InnoDB;

    create table ProjectMembership (
       membershipID bigint not null,
        organizationID bigint,
        role varchar(255),
        member_id bigint,
        project_id bigint,
        primary key (membershipID)
    ) engine=InnoDB;

    create table ProjectSnapshot (
       id bigint not null,
        organizationID bigint,
        effortLeft double precision not null,
        effortSpent double precision not null,
        originalEstimate double precision not null,
        projectSnapshotDate datetime(6),
        quotation double precision not null,
        project_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table ProjectTag (
       id bigint not null,
        organizationID bigint,
        tagKey varchar(255) not null,
        tagValue varchar(255) not null,
        project_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table RecursiveVacationRequest (
       id bigint not null,
        organizationID bigint,
        endDate date,
        endHalfDay integer,
        label varchar(255),
        startDate date,
        startHalfDay integer,
        status integer,
        applicant_id bigint,
        assignee_id bigint,
        parent_id bigint,
        recurrenceDay integer,
        primary key (id)
    ) engine=InnoDB;

    create table Report (
       id bigint not null,
        organizationID bigint,
        filterProject varchar(255),
        name varchar(50),
        type integer,
        primary key (id)
    ) engine=InnoDB;

    create table SubmittedTimesheet (
       id bigint not null,
        isValidated bit,
        week integer,
        year integer,
        account_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Task (
       id bigint not null,
        organizationID bigint,
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
        project_id bigint,
        taskType_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table Task_Batch (
       tasks_id bigint not null,
        batches_id bigint not null,
        primary key (tasks_id, batches_id)
    ) engine=InnoDB;

    create table TaskSnapshot (
       id bigint not null,
        organizationID bigint,
        effortLeft double precision not null,
        effortSpent double precision not null,
        originalEstimate double precision not null,
        snapshotDate datetime(6),
        assigned_id bigint,
        projectSnapshot_id bigint,
        task_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table TaskType (
       id bigint not null,
        organizationID bigint,
        enable bit,
        typeName varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table VacationRequest (
       id bigint not null,
        organizationID bigint,
        endDate date,
        endHalfDay integer,
        label varchar(255),
        startDate date,
        startHalfDay integer,
        status integer,
        applicant_id bigint,
        assignee_id bigint,
        parent_id bigint,
        primary key (id)
    ) engine=InnoDB;

    alter table Account 
       add constraint UK_l1aov0mnvpvcmg0ctq466ejwm unique (remoteSubject);

    alter table Imputation 
       add constraint UKsc0a68hjsx40d6xt9yep80o7l unique (day, task_id);

    alter table Organization 
       add constraint UK_griwilufaypfq6nxhupb1jfrv unique (name);

    alter table OrganizationMembership 
       add constraint UKpaqirhkf66d2aqtd9y6w8jn0p unique (member_id, organization_id);

    alter table Batch 
       add constraint FK21pv4fxo1jl876oc1u31wf21n 
       foreign key (project_id) 
       references Project (id);

    alter table CostByCategory 
       add constraint FKpeelsy07hkv1baei6fv1oo7s2 
       foreign key (account_id) 
       references Account (id);

    alter table DataTableConfig 
       add constraint FKmyycwm902xvsapnqmv1y3r4gj 
       foreign key (user_id) 
       references Account (id);

    alter table DefaultTask 
       add constraint FKih2ta8adxqd0nv2sx4cdkp2n 
       foreign key (organization_id) 
       references Organization (id);

    alter table Imputation 
       add constraint FKicayo4omi1a8krucb5t7kipva 
       foreign key (account_id) 
       references Account (id);

    alter table OrganizationMembership 
       add constraint FKif7ywhi6j3a20y5ului9p2bix 
       foreign key (member_id) 
       references Account (id);

    alter table OrganizationMembership 
       add constraint FKevb5cud2ia1prwjdqor09er57 
       foreign key (organization_id) 
       references Organization (id);

    alter table ProjectMembership 
       add constraint FK3wl3q3i14wuy156wafo33wlas 
       foreign key (member_id) 
       references Account (id);

    alter table ProjectMembership 
       add constraint FKapg94jqua2lbkjdb0kofxtnln 
       foreign key (project_id) 
       references Project (id);

    alter table ProjectSnapshot 
       add constraint FKfeonsxvox8vw2ckgx517uvncs 
       foreign key (project_id) 
       references Project (id);

    alter table ProjectTag 
       add constraint FKflkgw7xvdg8kc0gnjsj950con 
       foreign key (project_id) 
       references Project (id);

    alter table RecursiveVacationRequest 
       add constraint FK_khc4schw5r1a0hc48j4jwf075 
       foreign key (applicant_id) 
       references Account (id);

    alter table RecursiveVacationRequest 
       add constraint FK_7cxue1ajn4v17uktsuj8cxtpg 
       foreign key (assignee_id) 
       references Account (id);

    alter table RecursiveVacationRequest 
       add constraint FK_pdorxr0o13vxqxtt8lou3t7by 
       foreign key (parent_id) 
       references RecursiveVacationRequest (id);

    alter table SubmittedTimesheet 
       add constraint FK8cv07tq7it76qd26wfyewa15y 
       foreign key (account_id) 
       references Account (id);

    alter table Task 
       add constraint FK26uly7piek733vu0rvs6tkusr 
       foreign key (assigned_id) 
       references Account (id);

    alter table Task 
       add constraint FKkkcat6aybe3nbvhc54unstxm6 
       foreign key (project_id) 
       references Project (id);

    alter table Task 
       add constraint FKigksw4egslpbdevlab7ucu8lb 
       foreign key (taskType_id) 
       references TaskType (id);

    alter table Task_Batch 
       add constraint FK3alougowadwygx3bxx1ug2vqi 
       foreign key (batches_id) 
       references Batch (id);

    alter table Task_Batch 
       add constraint FKkbly41iq8y7y6nasf42mn3b1t 
       foreign key (tasks_id) 
       references Task (id);

    alter table TaskSnapshot 
       add constraint FKqlt7jdeboxt0lajph1uwq3l95 
       foreign key (assigned_id) 
       references Account (id);

    alter table TaskSnapshot 
       add constraint FKf0uknp6e8ohk8xok1ww5kece8 
       foreign key (projectSnapshot_id) 
       references ProjectSnapshot (id);

    alter table TaskSnapshot 
       add constraint FKq0s3frhsv5jms3r14ax9jtnnh 
       foreign key (task_id) 
       references Task (id);

    alter table VacationRequest 
       add constraint FKf501udnxw766cpsw730g0jhxn 
       foreign key (applicant_id) 
       references Account (id);

    alter table VacationRequest 
       add constraint FK28esu0dtlr0he4ie5j5oipsck 
       foreign key (assignee_id) 
       references Account (id);

    alter table VacationRequest 
       add constraint FK1tbhfs9wnfdwefermfv9611fx 
       foreign key (parent_id) 
       references RecursiveVacationRequest (id);
