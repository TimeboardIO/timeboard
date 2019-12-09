# Timeboard Cheats


## How to control database connexion ?

    jdbc:ds-list 
    
## How to generate dataset in my database?

    In Karaf shell, create a variable path to your sample-data.jar then resolve and start the bundle :
    myPathDatasetJar = XXXXXX/timeboard/sample-data/target/sample-data-1.0-SNAPSHOT.jar
    bundle:install file://$myPathDatasetJar
    bundle:resolve sample-data
    bundle:start sample-data

## Where is my database configuration ?

    [KRONOPS_HOME]/etc/org.ops4j.datasource-timeboard-core-ds.cfg

## How to create new accounts ?

    timeboard:add-account

## How to display active http endpoints ?

    http:list

## How to not redeploy everythings ?

This command auto redeploy new installed maven artifacts 

    dev:watch [bundle name]
