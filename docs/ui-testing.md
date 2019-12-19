# UI Testing

Timeboard use [Cypress](https://docs.cypress.io/api/api/table-of-contents.html) to automatize ui tests. 
To run code you must have Chrome or Chromium installed.
## Run tests
### Enable local connexion
*Cypress* does not work fine with *cognito*. So you have to enable local connexion in your application.properties

    timeboard.uitest=true
Then  relaunch timeboard app.
.
### Install cypress
Go to cypress ui tests folder and install cypress

    cd [Timeboard_src_folder]/ui-tests 
    npm install cypress --savedev
### Launch cypress app
    cypress open --project .
Cypress app is launched. Yo can run all tests by clicking "Run all specs"

### Launch cypress in console
    cypress run --project .
Cypress app is launched. Yo can run all tests by clicking "Run all specs"

