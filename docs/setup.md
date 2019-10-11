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

### Setup Apache Karaf


#### Run Apache Karaf 
Unzip **libs/apache-karaf-4.2.6.zip** archive any where on yout computer and keep in mind this location.
The output folder will be named "KARAF_ROOT" in this documentation.

With terminal, go to KARAF_ROOT/bin and run 

    cd KARAF_ROOT/bin/
    chmod 777 *
    
Next you can start Apache Karaf

    KARAF_ROOT/bin/karaf debug
    
#### Install Apache Karaf Webconsole

In Karaf shell, run 

    feature:install webconsole
 
Open your web browser and got to 

    http://localhost:8181/system/console
    
    username : karaf
    password : karaf

If you can access to Karaf webconsole, your installation is ready to deploy Timeboard !

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


Command is assumed to be run in Apache Karaf Shell :

    feature:repo-add mvn:timeboard/features/LATEST/xml


Command is assumed to be run in Apache Karaf Shell :

    feature:install timeboard-core 
    feature:install timeboard-home timeboard-projects timeboard-timesheet  
    feature:install timeboard-shell  

Open your web browser and got to 

    http://localhost:8181
    
Congratulation, you have access to Timeboard login page !

### Last job : create user account

Timeboard database is empty at first startup
In order to create your first user, you must use Apache Karaf Shell 

    timeboard:add-user -u timeboard -p pwd -e timeboard@localhost.com
        
Now, you can login into Timeboard with 

    username : timeboard
    password : pwd      
        

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

Releases for production use Apache Karaf KAR format.

A Release installation must follow classical KAR process : 

    https://karaf.apache.org/manual/latest/kar

