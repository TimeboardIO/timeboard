#!/usr/bin/env bash

mkdir -p build
mkdir -p temp
cp webapp/target/webapp-1.0-SNAPSHOT-spring-boot.jar temp/
cp -r scripts/aws/ temp
cd temp && zip timeboard.$TRAVIS_COMMIT.zip -r * .[^.]* && mv *.zip ../build/
