Feature: User can CRU tasks

  Scenario: A client can create a task
    Given user with an existing account and 1 projects
    When the user create a task
    Then the user has 1 task on project

  Scenario: B client can update a task
    Given user with an existing account and 1 projects
    Given the user create a task
    When the user update a task
    Then the user has 1 task on project
    Then the task have been updated





