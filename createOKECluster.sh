#!/bin/bash

if [[ $1 == "" ]]
then
  echo Required argument MSDATAWORKSHOP_COMPARTMENT_ID not provided. The compartmentid can be copied from the OCI Console.
  echo Usage example : ./createOKECluster.sh ocid1.compartment.oc1..aaaaaaaaxbvaatfz6dyfqbxhmasxfyui4rjek5dnzgcbivfwvsho77myfnqq
  exit
fi

echo ________________________________________
echo creating VCN and OKE cluster ...
echo ________________________________________

export WORKINGDIR=workingdir
echo creating working directory $WORKINGDIR to store values...
mkdir $WORKINGDIR

export MSDATAWORKSHOP_COMPARTMENT_ID=$1
echo $MSDATAWORKSHOP_COMPARTMENT_ID | tr -d '"' > $WORKINGDIR/msdataworkshopcompartmentid.txt
echo MSDATAWORKSHOP_COMPARTMENT_ID... $MSDATAWORKSHOP_COMPARTMENT_ID

echo creating vcn...
oci network vcn create --cidr-block 10.0.0.0/16 --compartment-id $MSDATAWORKSHOP_COMPARTMENT_ID --display-name "msdataworkshopvcn" | jq --raw-output '.data | .["id"] ' > $WORKINGDIR/msdataworkshopvcnid.txt
export MSDATAWORKSHOP_VCN_ID=$(cat $WORKINGDIR/msdataworkshopvcnid.txt)
echo MSDATAWORKSHOP_VCN_ID... $MSDATAWORKSHOP_VCN_ID

echo creating oke cluster...
oci ce cluster create --compartment-id $MSDATAWORKSHOP_COMPARTMENT_ID --kubernetes-version v1.16.8 --name msdataworkshopcluster --vcn-id $MSDATAWORKSHOP_VCN_ID

echo ________________________________________
echo OKE cluster is being provisioned. Check for status using verifyOKEAndCreateKubeConfig.sh script later...
echo ________________________________________

