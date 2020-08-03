#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo create atpaqadmin deployment and service...
export CURRENTTIME=$( date '+%F_%H:%M:%S' )
echo CURRENTTIME is $CURRENTTIME  ...this will be appended to generated deployment yaml

cp atpaqadmin-deployment.yaml atpaqadmin-deployment-$CURRENTTIME.yaml

#may hit sed incompat issue with mac
sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" atpaqadmin-deployment-${CURRENTTIME}.yaml
sed -i "s|%ORDER_PDB_NAME%|${ORDER_PDB_NAME}|g" atpaqadmin-deployment-${CURRENTTIME}.yaml
sed -i "s|%INVENTORY_PDB_NAME%|${INVENTORY_PDB_NAME}|g" atpaqadmin-deployment-${CURRENTTIME}.yaml

echo conducting setup required to create dblinks in OCI....
./setupForDBLinksInCloud.sh

echo  attempting to deploy atpaqadmin-deployment-${CURRENTTIME}.yaml

if [ -z "$1" ]; then
    kubectl apply -f $SCRIPT_DIR/atpaqadmin-deployment-${CURRENTTIME}.yaml -n msdataworkshop
else
    kubectl apply -f <(istioctl kube-inject -f $SCRIPT_DIR/atpaqadmin-deployment-${CURRENTTIME}.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/atpaqadmin-service.yaml -n msdataworkshop

