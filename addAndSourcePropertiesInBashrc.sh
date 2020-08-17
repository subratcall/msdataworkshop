#!/bin/bash

#echo "export MSDATAWORKSHOP_LOCATION=~/msdataworkshop-master ; source $MSDATAWORKSHOP_LOCATION/msdataworkshop.properties" >> ~/.bashrc
echo "export MSDATAWORKSHOP_LOCATION=~/msdataworkshop-master/" >> ~/.zprofile
echo "source ~/msdataworkshop-master/msdataworkshop.properties" >> ~/.zprofile
source ~/.bashrc

