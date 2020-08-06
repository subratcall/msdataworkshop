#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo create inventory-helidon deployment and service...

export CURRENTTIME=$( date '+%F_%H:%M:%S' )
echo CURRENTTIME is $CURRENTTIME  ...this will be appended to generated deployment yaml
cp inventory-helidon-deployment.yaml inventory-helidon-deployment-$CURRENTTIME.yaml

#may hit sed incompat issue with mac
sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" inventory-helidon-deployment-$CURRENTTIME.yaml
sed -i "s|%INVENTORY_PDB_NAME%|${INVENTORY_PDB_NAME}|g" inventory-helidon-deployment-$CURRENTTIME.yaml

if [ -z "$1" ]; then
    kubectl apply -f $SCRIPT_DIR/inventory-helidon-deployment-$CURRENTTIME.yaml -n msdataworkshop
else
    kubectl apply -f <(istioctl kube-inject -f $SCRIPT_DIR/inventory-helidon-deployment-$CURRENTTIME.yaml) -n msdataworkshop
fi