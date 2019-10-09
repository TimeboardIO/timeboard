FROM openjdk:11.0.4-stretch

COPY libs/apache-karaf-4.2.6.zip                 /timeboard/apache-karaf-4.2.6.zip

WORKDIR timeboard

RUN apt update && apt install unzip

RUN unzip apache-karaf-4.2.6

RUN apt clean

COPY features/target/features-1.0-SNAPSHOT.kar         /timeboard/apache-karaf-4.2.6/deploy/
COPY docs/sample/org.ops4j.datasource-timeboard-core-ds.cfg   /timeboard/apache-karaf-4.2.6/etc/org.ops4j.datasource-timeboard-core-ds.cfg

WORKDIR /timeboard/apache-karaf-4.2.6


CMD ./bin/karaf server