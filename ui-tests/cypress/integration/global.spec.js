
Cypress.Commands.add('login', () => {
    cy.visit('http://localhost:8080');

    cy.get('a.link')
        .click();

    cy.get('input[name=username]').type("user");

    cy.get('input[name=password]').type("password");

    cy.get('button[type=submit]')
        .click()  ;

    cy.url().then(url => {
        if(url.includes('select')) {
            cy.get('.button.ui.item').click();
        }
    });
});

beforeEach(function () {
    cy.login();
});


describe('Timeboard Test', function() {
    it('Connection user', function() {
        cy.url().should('include', '/home')
        cy.get('.right.menu .title').should('contain', 'user')
    });

});
