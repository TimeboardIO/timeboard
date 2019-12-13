# Installation

## For development

### Prerequisites

- Open JDK 11
- Nodejs
- Docker engine
- Maven
- Internet connexion
- MySQL Workbench
- Intellij or maven compatible IDE

### Download source code

    git clone https://github.com/timeboardio/timeboard.git


### Setup database

Run this docker command to provide database container and deploy database configurations. 
Commands are assumed to be run from source code root folder. 

    docker run -d --name timeboard-mysql \
        -v ${PWD}/scripts/sql:/docker-entrypoint-initdb.d \
        -e MYSQL_ROOT_PASSWORD=timeboard \
        -e MYSQL_DATABASE=timeboard \
        -p 3306:3306 \
        mysql:8.0
        
A shortcut is available 

    ./scripts/docker/mysql.sh
    

### Build Timeboard source code

Command is assumed to be run from source code root folder :

    mvn install -P full   
    
### Deploy Timeboard


Timeboard source code is divided into business modules. To run timeboard run **App** in webapp module.

    Run timeboard.webapp.App 

Open your web browser and got to 

    http://localhost:8080
    
Congratulation, you have access to Timeboard !

### Last job : Authentication account

Timeboard works with amazon cognito authentication.
If you just wan't to use timeboard for testing you can enable testing authentication in **application.properties** located in *webapp/src/main/ressources/*

    timeboard.uitest = true
        
Now, you can login into Timeboard with 

    username : user
    password : password     
        

## For test with Docker

### Prerequisites

- Docker
- Docker Compose
- Java
- Maven

First build source code with 

    mvn install
    
Build karaf Kar bundle

    cd features && mvn karaf:kar && cd ..
    
Build docker container

    docker build -t timeboardio/timeboard .
        
Run docker compose

    docker-compose up

## For production

Not ready yet, missing documentation :(

