# Technical Overview


## Big picture

Timeboard is based on Apache Karaf Runtime.

![alt text](images/deployment.png "Login form UI")

All Timeboard runtime dependencies can be found in Karaf [feature.xml](../features/src/main/feature/feature.xml) file


## Technical stack

### Server side

- Java JDK 11
- Apache Karaf 2.4.6
- OSGI R6
- Hibernate

### Client side 

- Thymeleaf
- Semantic-UI


## Timeboard components

- Core : business logic
- Core-ui : runtime framework for web ui
- Home : landing page, display graphs and user activities
- Projects : projects, tasks and clusters setup pages
- Timesheet : page used to note effort spent on projects
- Security : brind software security (filters, login form)
- Shell : custom karaf shell commands for Timeboard

## Functionnal basics

### Actor

Any logged user to Timeboard is an actor

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

### Project Clusters

Project Clusters is a tree of project. This concept is used to group project for reporting generation.

A project can be referenced by several project clusters.

