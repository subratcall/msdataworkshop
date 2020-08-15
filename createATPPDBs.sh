#!/bin/bash

echo ________________________________________
echo creating ATP PDBs ...
echo ________________________________________

export WORKINGDIR=workingdir
echo WORKINGDIR = $WORKINGDIR

export MSDATAWORKSHOP_COMPARTMENT_ID=$(cat $WORKINGDIR/msdataworkshopcompartmentid.txt)
echo console created compartment ...
echo MSDATAWORKSHOP_COMPARTMENT_ID... $MSDATAWORKSHOP_COMPARTMENT_ID

echo create order PDB...
oci db autonomous-database create --admin-password Welcome12345 --compartment-id $MSDATAWORKSHOP_COMPARTMENT_ID --cpu-core-count 1 --data-storage-size-in-tbs 1 --db-name ORDERDB | jq --raw-output '.data | .["id"] ' > $WORKINGDIR/msdataworkshoporderdbid.txt
export MSDATAWORKSHOP_ORDERDB_ID=$(cat $WORKINGDIR/msdataworkshoporderdbid.txt)
echo MSDATAWORKSHOP_ORDERDB_ID... $MSDATAWORKSHOP_ORDERDB_ID

echo create inventory PDB...
oci db autonomous-database create --admin-password Welcome12345 --compartment-id $MSDATAWORKSHOP_COMPARTMENT_ID --cpu-core-count 1 --data-storage-size-in-tbs 1 --db-name INVENTORYDB | jq --raw-output '.data | .["id"] ' > $WORKINGDIR/msdataworkshopinventorydbid.txt
export MSDATAWORKSHOP_INVENTORYDB_ID=$(cat $WORKINGDIR/msdataworkshopinventorydbid.txt)
echo MSDATAWORKSHOP_INVENTORYDB_ID... $MSDATAWORKSHOP_INVENTORYDB_ID
