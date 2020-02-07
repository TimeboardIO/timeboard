# Installation

## For development

### Prerequisites

- Open JDK 11
- Nodejs
- Docker engine
- Maven
- Internet connexion
- MySQL Workbench v6+
- Git 2 (and Git GUI client optional) 
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
        
For information database init script is available in  

    ./scripts/sql/mysql8.sql
    
To start mysql docker component 

    docker start timeboard-mysql

### Build Timeboard source code

Command is assumed to be run from source code root folder :

    mvn install -P full   
    
### Open IntelliJ IDE

sudo /snap/intellij-idea-community/202/bin/idea.sh

It can be convenient to make a symlink,
for example:
ln -s /snap/intellij-idea-community/202/bin//idea.sh /home/vagrant/idea.sh

Thus, you can use it, in vagrant's home directory:
sudo ./idea.sh 

### Import Timeboard project into IntelliJ

At IntelliJ startup:
-> Select 'Import Project', 
-> Select directory: '/home/vagrant/projects/timeboard'

In Import Project popup:
-> Select 'Import project fron external model' and select 'Maven'

### Deploy Timeboard

Timeboard source code is divided into business modules. To run timeboard run **App.main()** in webapp module from an IDE.

   {SRC_ROOT}/webapp/src/main/java/timeboard/webapp.App

Open your web browser and got to 

    http://localhost:8080
    
Congratulation, you have access to Timeboard !


### Trouble shooting
#### Authentication Error

If you are using a virtual machine, try to reset date/time settings of your device.
On ubuntu : parameters > details > date & time > click twice on switch automatic date & time settings 
        

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

