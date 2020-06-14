#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo delete supplier-helidon-se deployment and service...

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" supplier-helidon-se-deployment.yaml

if [ -z "$1" ]; then
    kubectl delete -f $SCRIPT_DIR/supplier-helidon-se-deployment.yaml -n msdataworkshop
else
    kubectl delete -f <(istioctl kube-inject -f $SCRIPT_DIR/supplier-helidon-se-deployment.yaml) -n msdataworkshop
fi

kubectl delete -f $SCRIPT_DIR/supplier-helidon-se-service.yaml -n msdataworkshop

