[![Build Status](https://travis-ci.com/pierremellet/kronops.svg?branch=develop)](https://travis-ci.com/pierremellet/kronops)

Prerequisites

    * Java JDK 11
    * OSX or Ubuntu (not tested on other OS)
    * Maven 3
    * Internet connection with an access to maven central  
    * Docker for mysql container


Download Karaf & uncompress archive
   
   
    http://www.apache.org/dyn/closer.lua/karaf/4.2.6/apache-karaf-4.2.6.tar.gz
    
Into Karaf home dir


    chmod 777 bin/*

Run Karaf in debug mode


    ./bin/karaf debug

Compile Kronops source code


    mvn clean install
    

Init Database

    start mysql container and create database named "kronops"
    use SQL script sql/mysql8.sql to load db schema

Add Kronops features repo to Karaf


    feature:repo-add mvn:kronops/features/LATEST/xml

Install Kronops
    
    
    feature:install kronops-datasource
    feature:install kronops-core 
    feature:install kronops-core-ui 
    feature:install kronops-security 
    feature:install kronops-home 
    feature:install kronops-projects 
    
    
Configure SSL (optional)

    into karaf install dir /etc

    keytool -genkey -keyalg RSA -validity 365 -alias serverkey -keypass password -storepass password -keystore keystore.jks
    keytool -genkey -keyalg RSA -validity 365 -alias clientkey -keypass password -storepass password -keystore client.jks
    keytool -export -rfc -keystore client.jks -storepass password -alias clientkey -file client.cer
    keytool -import -trustcacerts -keystore keystore.jks -storepass password -alias clientkey -file client.cer
    
    edit /etc/org.ops4j.pax.web.cfg and add 
    
    org.osgi.service.http.secure.enabled=true
    org.ops4j.pax.web.ssl.keystore=${karaf.etc}/keystore.jks
    org.ops4j.pax.web.ssl.password=password
    org.ops4j.pax.web.ssl.keypassword=password
        
   
       
    
Open webrowser 

    http://localhost:8181 
    
    or
    
    https://localhost:8443 (if http sercure enabled)