#!/bin/bash

SCRIPT_DIR=$(dirname $0)

IMAGE_NAME=order-helidon
IMAGE_VERSION=0.1

if [ -z "DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

eval "cat <<EOF
$(<$SCRIPT_DIR/order-helidon-atp-deployment.yaml)
EOF" > $SCRIPT_DIR/order-helidon-atp-deployment.yaml

export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

if [ -z "$1" ]; then
    kubectl apply -f $SCRIPT_DIR/order-helidon-atp-deployment.yaml -n msdataworkshop
else
    kubectl apply -f <(istioctl kube-inject -f $SCRIPT_DIR/order-helidon-atp-deployment.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/order-service.yaml -n msdataworkshop
