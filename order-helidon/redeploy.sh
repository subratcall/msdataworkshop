#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo redeploy order-helidon...

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" order-helidon-deployment.yaml


if [ -z "$1" ]; then
    kubectl delete -f $SCRIPT_DIR/order-helidon-deployment.yaml -n msdataworkshop
else
    kubectl delete -f <(istioctl kube-inject -f $SCRIPT_DIR/order-helidon-deployment.yaml) -n msdataworkshop
fi

if [ -z "$1" ]; then
    kubectl create -f $SCRIPT_DIR/order-helidon-deployment.yaml -n msdataworkshop
else
    kubectl create -f <(istioctl kube-inject -f $SCRIPT_DIR/order-helidon-deployment.yaml) -n msdataworkshop
fi


