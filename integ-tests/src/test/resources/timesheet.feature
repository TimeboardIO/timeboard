Feature: User add imputation on tasks and submit his timesheet

  Scenario: A client can add imputation on a task
    Given user with an existing account and 1 projects with 5 tasks
    When the user add imputations all days of task
    Then the user has imputations all tasks of week

  Scenario: A client can validate imputation on a task
    Given user with an existing account and 1 projects with 5 tasks
    Given the user add imputations all days of task
    When the user submit his timesheet
    Then the timesheet is submitted




