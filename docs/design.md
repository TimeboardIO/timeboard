# Technical Overview


## Big picture

Timeboard is based on Spring MVC.

![alt text](images/deployment.png "Login form UI")


## Technical stack

### Server side

- Java JDK 11
- Spring 5.1.x
- Hibernate

### Client side 

- Thymeleaf
- Semantic-UI
- Vue JS


## Timeboard components

- Core : business logic
- Webapp : Spring main headpoint 
- Core-ui : runtime framework for web ui
- Home : landing page, display graphs and account activities
- Account : account information edition
- Organization : all business about organizations
- Projects : projects, tasks and clusters setup pages
- Timesheet : page used to note effort spent on projects
- Reporting : Used to generate reports
- Theme : Timeboard semantic UI Stylesheets


## Functionnal basics

### Actor

Any logged account to Timeboard is an actor

### Project

A project is a set of tasks. Actors can be projet's member.
Each project member (an actor) as a project role.

Project role is used to allow or not some actions on project.

### Task

A task is time based job. A Task as a start date, a stop date and and original effort.
A task is assigned to a unique Actor.

### Milestone

A milestone is a set of tasks. A milestone is mainly defined by a name, a date and a type.
A project may have several milestones.

### Organization

Organization is a tenant on project, members, tasks. This concept is used to compartmentalize data of different tenants.

A project can be referenced by several project clusters.

