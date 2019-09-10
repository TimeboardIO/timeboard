[![Build Status](https://travis-ci.com/pierremellet/kronops.svg?branch=develop)](https://travis-ci.com/pierremellet/kronops)

Prerequisites

    * Java JDK 11
    * OSX or Ubuntu (not tested on other OS)
    * Maven 3
    * Internet connection with an access to maven central  


Download Karaf & uncompress archive
   
   
    http://www.apache.org/dyn/closer.lua/karaf/4.2.6/apache-karaf-4.2.6.tar.gz
    
Into Karaf home dir


    chmod 777 bin/*

Run Karaf in debug mode


    ./bin/karaf debug

Compile Kronops source code


    mvn clean install

Add Kronops features repo to Karaf


    feature:repo-add mvn:kronops/features/LATEST/xml

Install Kronops
    
    
    feature:install kronops-datasource
    feature:install kronops-core 
    feature:install kronops-rpcbridge 
    feature:install kronops-webui
    
Open webrowser 

    http://localhost:8181/kronops/home.html