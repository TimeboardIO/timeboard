

beforeEach(function () {
    cy.login();
    cy.visit('http://localhost:8080/timesheet');
});


describe('Timeboard Tests', function() {

    it('Timesheet Menu Test', function() {
        cy.contains("Timesheet")
            .click()  ;
    });

    it('Imputation on all week', function() {
        cy.get('input[name=task')
    });
});