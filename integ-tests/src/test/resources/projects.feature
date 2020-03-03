Feature: View current user project list on projects page

  Scenario: A client can create a project
    Given user with an existing account and 3 projects
    When the user create a project
    Then the user has 4 projects

  Scenario: A client can update a project
    Given user with an existing account and 3 projects
    When the user update a project
    Then the user has 3 projects
    Then the project has been updated

  Scenario: A client can archive a project
    Given user with an existing account and 3 projects
    When the user archive a project
    Then the project has been archived
