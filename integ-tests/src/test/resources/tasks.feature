Feature: User can CRUD tasks

  Scenario: A client can create a task
    Given user with an existing account and 1 projects
    When the user create a task
    Then the user has 1 task on project
