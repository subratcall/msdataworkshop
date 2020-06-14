#!/bin/bash

echo ________________________________________
echo building and pushing frontend-helidon...
echo ________________________________________
cd frontend-helidon
./build.sh
cd ../

echo maven install soda jar...

mvn install:install-file -Dfile=orajsoda-1.1.0.jar -DgroupId=com.oracle \
    -DartifactId=orajsoda -Dversion=1.1.0 -Dpackaging=jar
cd ../

echo ________________________________________
echo building and pushing atpaqadmin...
echo ________________________________________
cd atpaqadmin
./build.sh
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
