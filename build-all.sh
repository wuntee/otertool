#!/bin/bash

# Clear old build
rm -rf release/current && mkdir release/current

# OSX
mkdir release/current/oter-osx && mvn -f pom-osx.xml clean package && mv target/otertool*jar-with-dependencies.jar release/current/oter-osx/otertool.jar && cp resources/run-osx.sh release/current/oter-osx && pushd release/current && tar czvf oter-osx.tar.gz oter-osx && popd

# Linux x86
mkdir release/current/oter-linux-x86 && mvn -f pom-linux-x86.xml clean package && mv target/otertool*jar-with-dependencies.jar release/current/oter-linux-x86/otertool.jar && cp resources/run-linux.sh release/current/oter-linux-x86 && pushd release/current && tar czvf oter-linux-x86.tar.gz oter-linux-x86 && popd

# Linux x86_64
mkdir release/current/oter-linux-x86_64 && mvn -f pom-linux-x86_64.xml clean package && mv target/otertool*jar-with-dependencies.jar release/current/oter-linux-x86_64/otertool.jar && cp resources/run-linux.sh release/current/oter-linux-x86_64 && pushd release/current && tar czvf oter-linux-x86_64.tar.gz oter-linux-x86_64 && popd

# Clean 
mvn clean
