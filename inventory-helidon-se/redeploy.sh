#!/bin/bash
SCRIPT_DIR=$(dirname "$0")
DOCKER_REGISTRY="iad.ocir.io/maacloud/msdataworkshop"
IMAGE_NAME="inventory-helidon-se"
IMAGE_VERSION="0.1"
KUBE_NAMESPACE="msdataworkshop"

echo redeploy inventory-helidon-se...

sed "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g;s|%IMAGE_NAME%|${IMAGE_NAME}|g;s|%IMAGE_VERSION%|${IMAGE_VERSION}|g" inventory-helidon-se-deployment.yaml > deployment.yaml

if [ -z "$1" ]; then
    kubectl delete -f "$SCRIPT_DIR"/deployment.yaml -n $KUBE_NAMESPACE
else
    kubectl delete -f <(istioctl kube-inject -f "$SCRIPT_DIR"/deployment.yaml) -n $KUBE_NAMESPACE
fi

if [ -z "$1" ]; then
    kubectl create -f "$SCRIPT_DIR"/deployment.yaml -n $KUBE_NAMESPACE
else
    kubectl create -f <(istioctl kube-inject -f "$SCRIPT_DIR"/deployment.yaml) -n $KUBE_NAMESPACE
fi


