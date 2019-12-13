beforeEach(function () {
    cy.visit('http://localhost:8080');

    cy.get('a.link')
        .click();

    cy.get('input[name=username]').type("user");

    cy.get('input[name=password]').type("password");

    cy.get('button[type=submit]')
        .click()  ;

});


describe('Timeboard Test', function() {
    it('Connection user', function() {
        cy.url().should('include', '/home')
        cy.get('.right.menu .title').should('contain', 'user')
    });


    it('Timesheet Test', function() {
        cy.contains("Timesheet")
            .click()  ;
    });


});


describe('Project Test', function() {

    const projectName = 'My new project';

    beforeEach(function () {
        cy.contains("Project")
            .click()  ;

    });

    it('Create project', function() {
        cy.contains("Create Project")
            .click();

        cy.get('input[name=projectName]').type(projectName);

        cy.get('button[type=submit]')
            .click();

        cy.get('.ui.card .header').should('contain', 'user');

    });

    it('Archive project', function() {
        cy.get('.card').contains(projectName)
            .get('.a').contains('Archive')
                .click();

    });

    describe('Project Setup', function() {

        beforeEach(function () {
            cy.get('.card').contains('Timeboard') //projectName
                .get('.button').contains('Setup')
                .click();
        });

        it('Edit project config', function () {
            cy.get('input[name=name]').clear().type(projectName);
            cy.get('input[name=quotation]').clear().type(30000);
            cy.get('textarea[name=comments]').clear().type('My super new project is very awesome. ');
            cy.get('button[type=submit]')
                .click();
            cy.get('input[name=name]').should('be.value', projectName);
            cy.get('input[name=quotation]').should('be.value', 30000);
            cy.get('textarea[name=comments]').should('be', 'My super new project is very awesome. ');

        });
    });

});