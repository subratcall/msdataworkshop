#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo delete inventory-helidon deployment and service...

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" inventory-helidon-deployment.yaml

if [ -z "$1" ]; then
    kubectl delete -f $SCRIPT_DIR/inventory-helidon-deployment.yaml -n msdataworkshop
else
    kubectl delete -f <(istioctl kube-inject -f $SCRIPT_DIR/inventory-helidon-deployment.yaml) -n msdataworkshop
fi

kubectl delete -f $SCRIPT_DIR/inventory-service.yaml -n msdataworkshop
