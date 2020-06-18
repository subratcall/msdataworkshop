#!/bin/bash

SCRIPT_DIR=$(dirname $0)

IMAGE_NAME=inventory-python
IMAGE_VERSION=0.1

if [ -z "DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" inventory-python-deployment.yaml

export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

kubectl create -f $SCRIPT_DIR/inventory-python-deployment.yaml -n msdataworkshop

kubectl create -f $SCRIPT_DIR/inventory-service.yaml -n msdataworkshop
