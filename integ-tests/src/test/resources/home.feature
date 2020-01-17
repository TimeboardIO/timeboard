Feature: View current user project count on landing page

  Scenario: A client makes call to GET /home
    Given user with an existing account and 1 project
    When the user calls /home
    Then the user receives 1 project

  Scenario: B client doesn't see his projects outside current org
    Given user with an existing account and 1 project in 1 org (A) and 1 in an other org (B)
    When the user calls /home on org A
    Then the user receives 1 project from org A