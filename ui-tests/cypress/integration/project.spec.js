const projectName = 'My new project' + Math.floor(Math.random() * 9999);

beforeEach(function () {
    cy.login();
});


describe('Project Test', function() {

    beforeEach(function () {
        cy.visit('http://localhost:8080/projects');
    });

    it('Create project', function() {
        cy.get('body').then(($body) => {
            // synchronously query from body
            // to find which element was created
            if ($body.text().includes('No Projects ?')) {
                // input was found, do something else here
                return cy.get('a').contains('here');
            }
            // else assume it was textarea
            return cy.contains("New Project")
        }).click();

        cy.get('input[name=projectName]').type(projectName);
        cy.get('button[type=submit]')
            .click();
        cy.get('.ui.card .header').should('contain', projectName);
    });

    it('Edit project config', function () {

        cy.get('.card').contains(projectName) //projectName
            .get('.button').contains('Setup')
            .click();
        cy.get('input[name=name]').clear().type(projectName);
        cy.get('input[name=quotation]').clear().type(30000);
        cy.get('textarea[name=comments]').clear().type('My super new project is very awesome. ');
        cy.get('button[type=submit]')
            .click();
        cy.get('input[name=name]').should('be.value', projectName);
        cy.get('input[name=quotation]').should('contains.value', 30000);
        cy.get('textarea[name=comments]').should('be', 'My super new project is very awesome. ');
    });

    it('Create Task', function () {
        cy.visit('http://localhost:8080/projects');

        cy.get('.card:contains("'+projectName+'")').find('.button').contains('Setup')
            .click();

        cy.get('a').contains('Tasks')
            .click();

        let d1 = new Date();
        let d2 = new Date();
        d1.setDate(d1.getDate() - 5);
        d2.setDate(d2.getDate() + 5);
        cy.contains('New Task').click();
        cy.wait(500);
        cy.get('input[name=taskName]').clear().type("First Task");
        cy.get('textarea[name=taskComments]').clear().type("This my first wonderful task.");
        cy.get('input[name=taskOriginalEstimate]').clear().type(Math.round(Math.random() * 100) / 100);
        cy.get('input[name=taskStartDate]').clear().type(d1.toISOString().substr(0,10));
        cy.get('input[name=taskEndDate]').clear().type(d2.toISOString().substr(0,10));
        //  cy.get('input.prompt.assigned').clear().type("use");
        //  cy.get('.results').first().click();

        cy.get('div:contains("Request")').click({multiple: true, force: true});
        cy.wait(500);
        cy.get('td:contains("First Task")').should('contain', "First Task");
    });

    it('Archive project', function(){
        cy.visit('http://localhost:8080/projects');

        cy.get('.card:contains("'+projectName+'")').find('.button').contains('Archive')
            .click({force: true});
    })



});