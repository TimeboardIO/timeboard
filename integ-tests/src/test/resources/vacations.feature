Feature: User can Create / Delete / Accept / Reject a vacation

  Scenario: A client can create a new vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects
    When the user create a vacation
    Then the user has 1 pending vacations

  Scenario: A client can delete a pending vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 1 vacations
    When the user delete a pending vacation
    Then the user has 0 pending vacations
    Then the user has no imputations on his vacations

  Scenario: A client can accept a pending vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 2 vacations
    When the user accept a pending vacation
    Then the user has 1 pending vacations
    Then the user has 1 accepted vacations
    Then the user has imputations on his vacations

  Scenario: A client can reject a pending vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 2 vacations
    When the user reject a pending vacation
    Then the user has 1 pending vacations
    Then the user has 1 rejected vacations
    Then the user has no imputations on his vacations

  Scenario: A client can accept a pending vacation and reject an other
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 3 vacations
    When the user accept a pending vacation
    When the user reject a pending vacation
    Then the user has 1 pending vacations
    Then the user has 1 accepted vacations
    Then the user has 1 rejected vacations
    Then the user has imputations on his vacations

  Scenario: A client can delete a accepted vacation
    Given start and end date of vacations
    Given user with an existing account and 1 projects and 1 vacations
    When the user accept a pending vacation
    When the user delete a accepted vacation
    Then the user has 0 pending vacations
    Then the user has 0 accepted vacations
    Then the user has 0 rejected vacations
    Then the user has no imputations on his vacations



# Recursive Vacation

  Scenario: A client can create a new recursive vacation
    Given start and end date of recursive vacations
    Given user with an existing account and 1 projects
    When the user create a recursive vacation
    Then the user has 1 pending recursive vacations
    Then the user has no imputations on his recursive vacations

  Scenario: A client can reject a new recursive vacation
    Given start and end date of recursive vacations
    Given user with an existing account and 1 projects and 1 recursive vacations
    When the user reject a pending vacation
    Then the user has 0 pending recursive vacations
    Then the user has 1 rejected recursive vacations
    Then the user has no imputations on his recursive vacations

  Scenario: A client can accept a new recursive vacation
    Given start and end date of recursive vacations
    Given user with an existing account and 1 projects and 1 recursive vacations
    When the user accept a pending vacation
    Then the user has 0 pending recursive vacations
    Then the user has 1 accepted recursive vacations
    Then the user has imputations on his recursive vacations

  Scenario: A client can delete an accepted recursive vacation
    Given start and end date of recursive vacations
    Given user with an existing account and 1 projects and 1 recursive vacations
    When the user accept a pending vacation
    When the user delete a accepted vacation
    Then the user has 0 pending recursive vacations
    Then the user has 0 accepted recursive vacations
    Then the user has no imputations on his recursive vacations