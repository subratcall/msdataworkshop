#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo create atpaqadmin deployment and service...

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" atpaqadmin-deployment.yaml

if [ -z "$1" ]; then
    kubectl create -f $SCRIPT_DIR/atpaqadmin-deployment.yaml -n msdataworkshop
else
    kubectl create -f <(istioctl kube-inject -f $SCRIPT_DIR/atpaqadmin-deployment.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/atpaqadmin-service.yaml -n msdataworkshop

