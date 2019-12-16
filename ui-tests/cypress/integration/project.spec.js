
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

Cypress.Commands.add('createProject', (projectName) => {
    cy.get('body').then(($body) => {
        // synchronously query from body
        // to find which element was created
        if ($body.text().includes('No Projects ?')) {
            // input was found, do something else here
            return cy.get('a').contains('here');
        }
        // else assume it was textarea
        return cy.contains("Create Project")
    }).click();

    cy.get('input[name=projectName]').type(projectName);
    cy.get('button[type=submit]')
        .click();
    cy.get('.ui.card .header').should('contain', projectName);

});

Cypress.Commands.add('archiveProject', (projectName) => {
    cy.get('.card').contains(projectName)
        .get('.button').contains('Archive')
        .click();

});


beforeEach(function () {
    cy.login();
});



describe('Project Test', function() {

    const projectName = 'My new project';

    beforeEach(function () {
        cy.contains("Project")
            .click()  ;

    });
/*
    it('Create project', function() {
       cy.createProject(projectName);
       cy.archiveProject(projectName);
    });
*/
/*
    describe('Project Setup', function() {

        beforeEach(function () {
            cy.createProject(projectName);

            cy.get('.card').contains(projectName) //projectName
                .get('.button').contains('Setup')
                .click();
        });

        afterEach(function () {
            cy.visit('http://localhost:8080');
            cy.contains("Project")
                .click();
            cy.archiveProject(projectName);
        });
        it('Edit project config', function () {
            cy.get('input[name=name]').clear().type(projectName);
            cy.get('input[name=quotation]').clear().type(30000);
            cy.get('textarea[name=comments]').clear().type('My super new project is very awesome. ');
            cy.get('button[type=submit]')
                .click();
            cy.get('input[name=name]').should('be.value', projectName);
            cy.get('input[name=quotation]').should('contains.value', 30000);
            cy.get('textarea[name=comments]').should('be', 'My super new project is very awesome. ');
        });


    });
*/

    describe('Project Tasks', function() {

        beforeEach(function () {
            cy.createProject(projectName);

            cy.get('.card').contains(projectName) //projectName
                .get('.button').contains('Setup')
                .click();

            cy.get('a').contains('Tasks')
                .click();
        });

        afterEach(function () {
            cy.visit('http://localhost:8080');
            cy.contains("Project")
                .click();
            cy.archiveProject(projectName);
        });
        it('Create Task', function () {
            cy.get('a').contains('New Task').click();
            cy.get('input[name=taskName]').clear().type("First Task");
            cy.get('textarea[name=taskComments]').clear().type("This my first wonderful task.");
            cy.get('input[name=taskOriginalEstimate]').clear().type(Math.random());
            cy.get('input[name=taskStartDate]').clear().type("2019-12-06");
            cy.get('input[name=taskEndDate]').clear().type("2019-12-20");
            cy.get('input[name=taskAssignedSearch]').clear().type("use");
            cy.get('.results').first().click();
            cy.get('select[name=taskTypeID]').select('Bug')
        });


    });


});