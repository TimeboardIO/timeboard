Feature: User can Create / Delete / Approve / Deny a vacation

  Scenario: A client can create a new vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects
    When the user create a vacation
    Then the user has 1 vacations
    Then the user has 1 pending vacations

  Scenario: A client can delete a vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 1 vacations
    When the user delete a pending vacation
    Then the user has 0 vacations
    Then the user has 0 pending vacations
    Then the user has no imputations on his vacations

  Scenario: A client can approve a vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 2 vacations
    When the user approve a pending vacation
    Then the user has 2 vacations
    Then the user has 1 pending vacations
    Then the user has 1 accepted vacations
    Then the user has imputations on his vacations

  Scenario: A client can deny a vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 2 vacations
    When the user deny a pending vacation
    Then the user has 2 vacations
    Then the user has 1 pending vacations
    Then the user has 1 rejected vacations
    Then the user has no imputations on his vacations

  Scenario: A client can approve a vacation and deny an other
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 3 vacations
    When the user approve a pending vacation
    When the user deny a pending vacation
    Then the user has 3 vacations
    Then the user has 1 pending vacations
    Then the user has 1 accepted vacations
    Then the user has 1 rejected vacations
    Then the user has imputations on his vacations