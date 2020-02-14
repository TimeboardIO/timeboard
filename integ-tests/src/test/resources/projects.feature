Feature: View current user project list on projects page

  Scenario: A client can create a project
    Given user with an existing account and 3 projects
    When the user create a project
    Then the user has 4 projects
