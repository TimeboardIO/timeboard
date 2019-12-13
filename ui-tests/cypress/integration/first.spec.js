describe('Post Resource', function() {
    it('Creating a New Post', function() {
        cy.visit('http://localhost:8080')     // 1.

        cy.get('a.link') // 2.
            .click()   // 3.

       /* cy.get('input.post-body')  // 4.
            .type('Hello, world!')   // 5.

        cy.contains('Submit')      // 6.
            .click()                 // 7.

        cy.url()                   // 8.
            .should('include', '/posts/my-first-post')

        cy.get('h1')               // 9.
            .should('contain', 'My First Post')*/
    })
})