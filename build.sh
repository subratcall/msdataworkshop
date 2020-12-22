#!/bin/bash

echo ________________________________________
echo building and pushing frontend-helidon...
echo ________________________________________
cd frontend-helidon
./build.sh
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
echo building and pushing supplier-helidon-se...
echo ________________________________________
cd supplier-helidon-se
./build.sh
cd ../

echo ________________________________________
echo building and pushing inventory-helidon...
echo ________________________________________
cd inventory-helidon
./build.sh
cd ../

echo ________________________________________
echo building and pushing inventory-python...
echo ________________________________________
cd inventory-python
./build.sh
cd ../

echo ________________________________________
echo building and pushing inventory-nodejs...
echo ________________________________________
cd inventory-nodejs
./build.sh
cd ../

echo ________________________________________
echo building and pushing inventory-helidon-se...
echo ________________________________________
cd inventory-helidon-se
./build.sh
cd ../

echo ________________________________________
echo ...finished
echo ________________________________________
