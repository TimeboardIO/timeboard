


beforeEach(function () {
    cy.login();
});


describe('Timeboard Test', function() {
    it('Connection user', function() {
        cy.url().should('include', '/home')
        cy.get('.right.menu .title').should('contain', 'user')
    });



});
