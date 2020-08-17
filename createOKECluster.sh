#!/bin/bash

if [[ $1 == "" ]]
then
  echo Required argument MSDATAWORKSHOP_COMPARTMENT_ID not provided. The compartmentid can be copied from the OCI Console.
  echo Usage example : ./createOKECluster.sh ocid1.compartment.oc1..aaaaaaaaxbvaatfz6dyfqbxhmasxfyui4rjek5dnzgcbivfwvsho77myfnqq
  echo [optional second argument is for specifying region. The default value is us-ashburn-1]
  exit
fi

echo ________________________________________
echo creating VCN and OKE cluster ...
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

echo creating vcn...
oci network vcn create --cidr-block 10.0.0.0/16 --compartment-id $MSDATAWORKSHOP_COMPARTMENT_ID --display-name "msdataworkshopvcn" | jq --raw-output '.data | .["id"] ' > $WORKINGDIR/msdataworkshopvcnid.txt
export MSDATAWORKSHOP_VCN_ID=$(cat $WORKINGDIR/msdataworkshopvcnid.txt)
echo MSDATAWORKSHOP_VCN_ID... $MSDATAWORKSHOP_VCN_ID

echo creating oke cluster...
oci ce cluster create --compartment-id $MSDATAWORKSHOP_COMPARTMENT_ID --kubernetes-version v1.16.8 --name msdataworkshopcluster --vcn-id $MSDATAWORKSHOP_VCN_ID

echo ________________________________________
echo OKE cluster is being provisioned. You will check for status using verifyOKEAndCreateKubeConfig.sh script later...
echo ________________________________________

