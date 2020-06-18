#!/bin/bash

echo ________________________________________
echo redeploying frontend-helidon...
echo ________________________________________
cd frontend-helidon
./redeploy.sh
cd ../

echo ________________________________________
echo redeploying atpaqadmin...
echo ________________________________________
cd atpaqadmin
./redeploy.sh
cd ../

echo ________________________________________
echo redeploying order-helidon...
echo ________________________________________
cd order-helidon
./redeploy.sh
cd ../

echo ________________________________________
echo redeploying inventory-helidon...
echo ________________________________________
cd inventory-helidon
./redeploy.sh
cd ../

echo ________________________________________
echo redeploying inventory-python...
echo ________________________________________
cd inventory-python
./redeploy.sh
cd ../

echo ________________________________________
echo redeploying supplier-helidon-se...
echo ________________________________________
cd supplier-helidon-se
./redeploy.sh
cd ../

echo ________________________________________
echo redeploying orderstreaming-helidon-se...
echo ________________________________________
cd orderstreaming-helidon-se
./redeploy.sh
cd ../

echo ________________________________________
echo ...finished
echo ________________________________________

