#!/bin/bash
SCRIPT_DIR=$(dirname "$0")
DOCKER_REGISTRY="iad.ocir.io/maacloud/msdataworkshop"
IMAGE_NAME="inventory-helidon-se"
IMAGE_VERSION="0.1"
KUBE_NAMESPACE="msdataworkshop"

if [ -z "$DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

sed "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g;s|%IMAGE_NAME%|${IMAGE_NAME}|g;s|%IMAGE_VERSION%|${IMAGE_VERSION}|g" inventory-helidon-se-deployment.yaml > deployment.yaml

export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

if [ -z "$1" ]; then
    kubectl create -f "$SCRIPT_DIR"/deployment.yaml -n $KUBE_NAMESPACE
else
    kubectl create -f <(istioctl kube-inject -f "$SCRIPT_DIR"/deployment.yaml) -n $KUBE_NAMESPACE
fi

kubectl create -f "$SCRIPT_DIR"/inventory-helidon-se-service.yaml -n $KUBE_NAMESPACE