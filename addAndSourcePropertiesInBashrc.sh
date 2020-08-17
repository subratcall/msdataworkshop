#!/bin/bash

# we put a placeholder msdataworkshopjaegeraddress.txt as we wont have the Jaeger LB address until later/setJaegerAddress.sh step
touch workingdir/msdataworkshopjaegeraddress.txt
echo "export MSDATAWORKSHOP_LOCATION=~/msdataworkshop-master/" >> ~/.bashrc
echo "source ~/msdataworkshop-master/msdataworkshop.properties" >> ~/.bashrc
source ~/.bashrc

