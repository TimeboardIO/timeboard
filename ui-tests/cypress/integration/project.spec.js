beforeEach(function () {

    cy.login();
});

const projectName = 'New project' + Math.floor(Math.random() * 999999);
const taskName ="Task "+ Math.floor(Math.random() * 999999);


describe('Project Test', function() {
    beforeEach(function () {
        cy.visit('http://localhost:8080/projects');
    });
    it('Save parameters', function() {
        cy.writeFile("cypress/fixtures/projects.json", {projectName: projectName, taskName: taskName});
    });
    it('Create project', function() {
        cy.get('[data-cy=create-project]')
            .click();

        cy.get('[data-cy=project-name]').type(projectName);
        cy.get('[data-cy=submit]')
            .click();
        cy.get('[data-cy=project-title]').should('contain', projectName);
    });

    it('Edit project config', function () {

        cy.get('[data-cy=project]:contains("'+projectName+'")').find('[data-cy=setup]')
            .click();
        cy.get('[data-cy=project-name]').clear().type(projectName);
        cy.get('[data-cy=project-quotation]').clear().type(30000);
        cy.get('[data-cy=project-description]').clear().type('My super new project is very awesome. ');
        cy.get('[data-cy=submit]')
            .click();
        cy.get('[data-cy=project-name]').should('be.value', projectName);
        cy.get('[data-cy=project-quotation]').should('contains.value', 30000);
        cy.get('[data-cy=project-comments]').should('be', 'My super new project is very awesome. ');
    });

    it('Create Task', function () {

        cy.visit('http://localhost:8080/projects');

        cy.get('[data-cy=project]:contains("'+projectName+'")').find('[data-cy=setup]')
            .click();

        cy.get('[data-cy=project-menu-tasks]')
            .click();

        let d1 = new Date();
        let d2 = new Date();
        d1.setDate(d1.getDate() - 5);
        d2.setDate(d2.getDate() + 5);
        cy.contains('New Task').click();
        cy.wait(500);
        cy.get('[data-cy=task-name]').clear().type(taskName);
        cy.get('[data-cy=task-comments]').clear().type("This my wonderful "+taskName+".");
        cy.get('[data-cy=task-oe]').clear().type(Math.round(Math.random() * 100) / 100);
        cy.get('[data-cy=task-start-date]').clear().type(d1.toISOString().substr(0,10));
        cy.get('[data-cy=task-end-date]').clear().type(d2.toISOString().substr(0,10));
        //  cy.get('input.prompt.assigned').clear().type("use");
        //  cy.get('.results').first().click();

        cy.get('[data-cy=task-submit]').click({multiple: true, force: true});
        cy.wait(500);
        cy.get('td:contains('+taskName+')').should('contain', taskName);
    });


    it('Approve Task', function () {
        cy.get('[data-cy=project]:contains("'+projectName+'")').find('[data-cy=setup]')
            .click();
        cy.get('[data-cy=project-menu-tasks]')
            .click();

        cy.get('[data-cy=approve-task]').click({multiple: true, force: true});

    });

    it('Archive project', function(){
        cy.visit('http://localhost:8080/projects');

        cy.get('[data-cy=project]:contains("'+projectName+'")').find('[data-cy=archive]')
            .click({force: true});
    })



});
