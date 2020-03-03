Feature: User can CRU tasks

  Scenario: A client can create a task
    Given user with an existing account and 1 projects
    When the user create a task
    Then the user has 1 pending task on project

  Scenario: B client can update a task
    Given user with an existing account and 1 projects
    Given the user create a task
    When the user update a task
    Then the user has 0 pending task on project
    Then the user has 1 in progress task on project
    Then the task has been updated

  Scenario: A client can accept a task
    Given user with an existing account and 1 projects with 1 tasks
    When the user accept a task
    Then the user has 0 pending task on project
    Then the user has 0 refused task on project
    Then the user has 1 in progress task on project
    Then the user has 0 done task on project
    Then the user has 0 archived task on project

  Scenario: A client can deny a task
    Given user with an existing account and 1 projects with 1 tasks
    When the user deny a task
    Then the user has 0 pending task on project
    Then the user has 1 refused task on project
    Then the user has 0 in progress task on project
    Then the user has 0 done task on project
    Then the user has 0 archived task on project

  Scenario: A client can archive a task
    Given user with an existing account and 1 projects with 1 tasks
    When the user archive a task
    Then the user has 0 pending task on project
    Then the user has 0 refused task on project
    Then the user has 0 in progress task on project
    Then the user has 0 done task on project
    Then the user has 1 archived task on project