#!/bin/bash
SCRIPT_DIR=$(dirname "$0")

echo create inventory-helidon-se deployment...
export CURRENTTIME=$( date '+%F_%H:%M:%S' )
echo CURRENTTIME is $CURRENTTIME  ...this will be appended to generated deployment yaml

cp inventory-helidon-se-deployment.yaml inventory-helidon-se-deployment-$CURRENTTIME.yaml

IMAGE_NAME="inventory-helidon-se"
IMAGE_VERSION="0.1"

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g;s|%IMAGE_NAME%|${IMAGE_NAME}|g;s|%IMAGE_VERSION%|${IMAGE_VERSION}|g"  inventory-helidon-se-deployment-${CURRENTTIME}.yaml
sed -i "s|%INVENTORY_PDB_NAME%|${INVENTORY_PDB_NAME}|g" inventory-helidon-se-deployment-${CURRENTTIME}.yaml

if [ -z "$1" ]; then
    kubectl apply -f "$SCRIPT_DIR"/inventory-helidon-se-deployment-${CURRENTTIME}.yaml -n msdataworkshop
else
    kubectl apply -f <(istioctl kube-inject -f "$SCRIPT_DIR"/inventory-helidon-se-deployment-${CURRENTTIME}.yaml) -n msdataworkshop
fi
