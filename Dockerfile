FROM openjdk:11.0.4-stretch

COPY libs/apache-karaf-4.2.6.zip                 /kronops/apache-karaf-4.2.6.zip

WORKDIR kronops

RUN apt update && apt install unzip

RUN unzip apache-karaf-4.2.6

RUN apt clean

COPY features/target/features-1.0-SNAPSHOT.kar         /kronops/apache-karaf-4.2.6/deploy/
COPY docs/sample/org.ops4j.datasource-kronops-core-ds.cfg   /kronops/apache-karaf-4.2.6/etc/org.ops4j.datasource-kronops-core-ds.cfg

WORKDIR /kronops/apache-karaf-4.2.6


CMD ./bin/karaf server