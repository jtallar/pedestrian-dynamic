#!/bin/bash
mvn -q clean install || { echo 'mvn clean install failed' ; exit 1 ; }
cd ./target
tar -xzf tp4-simu-1.0-bin.tar.gz
chmod u+x tp4-simu-1.0/*.sh
cd ..
