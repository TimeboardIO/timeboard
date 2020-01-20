




describe('Timeboard Test', function() {
    beforeEach(function () {
        cy.login();
    });

    it('Connection user', function() {
         cy.url().should('include', '/home')
         cy.fixture("user.json").then((user) => {
            cy.get('.right.menu .title').should('contain', user.email)
         });
    });

});
