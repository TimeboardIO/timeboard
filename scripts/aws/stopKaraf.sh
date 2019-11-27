#!/usr/bin/env bash

rm -rf /etc/karaf/apache-karaf-4.2.7/deploy/*
sudo /etc/karaf/apache-karaf-4.2.7/bin/client kar:uninstall features-1.0-SNAPSHOT; exit 0
/etc/karaf/apache-karaf-4.2.7/bin/stop; exit 0