#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo delete frontend-helidon deployment and service...

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" frontend-helidon-deployment.yaml

if [ -z "$1" ]; then
    kubectl delete -f $SCRIPT_DIR/frontend-helidon-deployment.yaml -n msdataworkshop
else
    kubectl delete -f <(istioctl kube-inject -f $SCRIPT_DIR/frontend-helidon-deployment.yaml) -n msdataworkshop
fi

kubectl delete -f $SCRIPT_DIR/frontend-helidon-service.yaml -n msdataworkshop

