#!/usr/bin/env bash

mvn clean install
mkdir -p build
mkdir -p temp
cp webapp/target/webapp-1.0-SNAPSHOT-spring-boot.jar temp/
cp -r scripts/aws/ temp
cd temp && zip timeboard.$(git rev-parse HEAD).zip -r * .[^.]* && mv *.zip ../build/
cd ..
ls -al build/*.zip

sed 's/^timeboard.commit.id=.*/timeboard.commit.id='$(git rev-parse HEAD)'/' webapp/src/main/resources/application.properties