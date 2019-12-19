



describe('Timeboard Tests', function() {
    beforeEach(function () {
        cy.login();
        cy.visit('http://localhost:8080/timesheet');
    });

    it('Imputation on on today ', function() {
        cy.fixture("projects.json").then((json) => {

           cy.get('[data-cy="'
                + json.projectName+'/'
                + json.taskName+'/'
                + new Date().toISOString().substr(0,10)+'"')
                .clear().type(1.0);
            cy.get('td').first().click();          // Click on button

            cy.get('[data-cy="'
                + json.projectName+'/'
                + json.taskName+'/'
                + new Date().toISOString().substr(0,10)+'"').parent().should('not.have.class', 'loading');
        })
    });
});