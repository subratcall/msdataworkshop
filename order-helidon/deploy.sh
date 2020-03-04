#!/bin/bash

SCRIPT_DIR=$(dirname $0)

IMAGE_NAME=order-helidon
IMAGE_VERSION=0.1

if [ -z "DEMOREGISTRY" ]; then
    echo "Error: DEMOREGISTRY env variable needs to be set!"
    exit 1
fi

export IMAGE=${DEMOREGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

if [ -z "$1" ]; then
    kubectl create -f $SCRIPT_DIR/frontend-helidon-deployment.yaml -n msdataworkshop
else
    kubectl create -f <(istioctl kube-inject -f $SCRIPT_DIR/frontend-helidon-deployment.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/frontend-service.yaml -n msdataworkshop
