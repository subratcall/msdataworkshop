#!/bin/bash

SCRIPT_DIR=$(dirname $0)

IMAGE_NAME=inventory-helidon
IMAGE_VERSION=0.1

if [ -z "DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

cp inventory-helidon-deployment.yaml inventory-helidon-deployment.yaml

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" inventory-helidon-deployment.yaml
sed -i "s|%INVENTORY_PDB_NAME%|${INVENTORY_PDB_NAME}|g" inventory-helidon-deployment.yaml

export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

if [ -z "$1" ]; then
    kubectl create -f $SCRIPT_DIR/inventory-helidon-deployment.yaml -n msdataworkshop
else
    kubectl create -f <(istioctl kube-inject -f $SCRIPT_DIR/inventory-helidon-deployment.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/inventory-service.yaml -n msdataworkshop
