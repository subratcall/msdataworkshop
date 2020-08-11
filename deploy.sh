#!/bin/bash

echo ________________________________________
echo deploying frontend-helidon...
echo ________________________________________
cd frontend-helidon
./deploy.sh
cd ../

echo ________________________________________
echo deploying atpaqadmin...
echo ________________________________________
cd atpaqadmin
./deploy.sh
cd ../

echo ________________________________________
echo deploying order-helidon...
echo ________________________________________
cd order-helidon
./deploy.sh
cd ../

echo ________________________________________
echo deploying supplier-helidon-se...
echo ________________________________________
cd supplier-helidon-se
./deploy.sh
cd ../

echo ________________________________________
echo deploying inventory-helidon...
echo ________________________________________
cd inventory-helidon
./deploy.sh
cd ../

# note that this creates only the inventory helidon (MP) deployment

echo ________________________________________
echo ...finished
echo ________________________________________
