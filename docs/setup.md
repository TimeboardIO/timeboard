# Installation

## For development

### Prerequisites

- Open JDK 11
- Docker engine
- Internet connexion
- MySQL Workbench
- Intellij

### Download source code

    git clone https://github.com/pierremellet/kronops.git

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

If you can access to Karaf webconsole, your installation is ready to deploy Kronops !

### Setup database

Run this docker command to provide database container and deploy database configurations. 
Commands are assumed to be run from source code root folder. 

    docker run -d --name kronops-mysql \
        -v ${PWD}/scripts/sql:/docker-entrypoint-initdb.d \
        -e MYSQL_ROOT_PASSWORD=kronops \
        -e MYSQL_DATABASE=kronops \
        -p 3306:3306 \
        mysql:8.0
        
A shortcut is available 

    ./scripts/docker/mysql.sh
    

### Build Kronops source code

Command is assumed to be run from source code root folder :

    mvn install    
    
Command is assumed to be run in Apache Karaf Shell :

    feature:repo-add mvn:kronops/features/LATEST/xml

### Deploy Kronops


Command is assumed to be run in Apache Karaf Shell :

    feature:install kronops-core 
    feature:install kronops-home kronops-projects kronops-timesheet  
    feature:install kronops-shell  

Open your web browser and got to 

    http://localhost:8181
    
Congratulation, you have access to Kronops login page !

### Last job : create user account

Kronops database is empty at first startup
In order to create your first user, you must use Apache Karaf Shell 

    kronops:add-user -u kronops -p pwd -e kronops@localhost.com
        
Now, you can login into Kronops with 

    username : kronops
    password : pwd      
        
## For production

Not ready yet :(