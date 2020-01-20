// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

Cypress.Commands.add('login', () => {

    cy.clearLocalStorage();

    cy.clearCookies();

    cy.visit('http://localhost:8080/oauth2/authorization/cognito');

    cy.url().then(url => {
        if(url.includes('login')) {
            cy.fixture("user.json").then((user) => {
                cy.get('.visible-md input[name=username]').type(user.login);
                cy.get('.visible-md input[name=password]').type(user.password);
                cy.get('.visible-md input[name=signInSubmitButton]').click();
            });
        }
    });

    cy.url().then(url => {
        if(url.includes('select')) {
            cy.get('[data-cy=select-org-submit]').click();
        }
    });
});