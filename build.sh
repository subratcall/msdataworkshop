#!/bin/bash

echo ________________________________________
echo building and pushing frontend-helidon...
echo ________________________________________
cd frontend-helidon
./build.sh
cd ../

echo ________________________________________
echo building and pushing osb-atp-dbadmin-helidon...
echo ________________________________________
cd osb-atp-dbadmin-helidon
./build.sh
cd ../


echo maven install aq and soda jars...
cd lib
mvn install:install-file -Dfile=aqapi-20.1.jar -DgroupId=com.oracle \
    -DartifactId=aqapi -Dversion=20.1 -Dpackaging=jar

mvn install:install-file -Dfile=orajsoda-1.1.0.jar -DgroupId=com.oracle \
    -DartifactId=orajsoda -Dversion=1.1.0 -Dpackaging=jar
cd ../

echo ________________________________________
echo building and pushing order-helidon...
echo ________________________________________
cd order-helidon
./build.sh
cd ../

echo ________________________________________
echo building and pushing inventory-helidon...
echo ________________________________________
cd inventory-helidon
./build.sh
cd ../

echo ________________________________________
echo building and pushing supplier-helidon-se...
echo ________________________________________
cd supplier-helidon-se
./build.sh
cd ../

echo ________________________________________
echo building and pushing orderstreaming-helidon-se...
echo ________________________________________
cd orderstreaming-helidon-se
./build.sh
cd ../

echo ________________________________________
echo ...finished
echo ________________________________________
