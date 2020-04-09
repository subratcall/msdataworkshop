#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo delete orderstreaming-helidon-se deployment and service...

sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" orderstreaming-helidon-se-deployment.yaml

if [ -z "$1" ]; then
    kubectl delete -f $SCRIPT_DIR/orderstreaming-helidon-se-deployment.yaml -n msdataworkshop
else
    kubectl delete -f <(istioctl kube-inject -f $SCRIPT_DIR/orderstreaming-helidon-se-deployment.yaml) -n msdataworkshop
fi

kubectl delete -f $SCRIPT_DIR/orderstreaming-helidon-se-service.yaml -n msdataworkshop
