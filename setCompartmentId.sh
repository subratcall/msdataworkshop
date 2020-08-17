#!/bin/bash

if [[ $1 == "" ]]
then
  echo Required argument MSDATAWORKSHOP_COMPARTMENT_ID not provided. The compartmentid can be copied from the OCI Console.
  echo Usage example : ./setCompartmentId.sh ocid1.compartment.oc1..aaaaaaaaxbvaatfz6dyfqbxhmasxfyui4rjek5dnzgcbivfwvsho77myfnqq
  echo [optional second argument is for specifying region. The default value is us-ashburn-1]
  exit
fi

echo ________________________________________
echo setting compartmentid and region ...
echo ________________________________________

export WORKINGDIR=workingdir
echo creating working directory $WORKINGDIR to store values...
mkdir $WORKINGDIR


export MSDATAWORKSHOP_REGION=$2
if [[ $MSDATAWORKSHOP_REGION == "" ]]
then
  echo defaulting to region us-ashburn-1
  export MSDATAWORKSHOP_REGION=us-ashburn-1
fi
echo $MSDATAWORKSHOP_REGION | tr -d '"' > $WORKINGDIR/msdataworkshopregion.txt
echo MSDATAWORKSHOP_REGION... $MSDATAWORKSHOP_REGION


export MSDATAWORKSHOP_COMPARTMENT_ID=$1
echo $MSDATAWORKSHOP_COMPARTMENT_ID | tr -d '"' > $WORKINGDIR/msdataworkshopcompartmentid.txt
echo MSDATAWORKSHOP_COMPARTMENT_ID... $MSDATAWORKSHOP_COMPARTMENT_ID
