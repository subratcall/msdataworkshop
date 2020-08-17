#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo create frontend deployment and service...
export CURRENTTIME=$( date '+%F_%H:%M:%S' )
echo CURRENTTIME is $CURRENTTIME  ...this will be appended to generated deployment yaml
echo DOCKER_REGISTRY is $DOCKER_REGISTRY

cp frontend-helidon-deployment.yaml frontend-helidon-deployment-$CURRENTTIME.yaml

#may hit sed incompat issue with mac
sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" frontend-helidon-deployment-$CURRENTTIME.yaml

if [ -z "$1" ]; then
    kubectl apply -f $SCRIPT_DIR/frontend-helidon-deployment-$CURRENTTIME.yaml -n msdataworkshop
else
    kubectl apply -f <(istioctl kube-inject -f $SCRIPT_DIR/frontend-helidon-deployment-$CURRENTTIME.yaml) -n msdataworkshop
fi

echo creating frontend loadbalancer service
echo "AlreadyExists" error is expected if installGraalVMJaegerAndFrontendLB.sh was previous run...

kubectl create -f $SCRIPT_DIR/frontend-service.yaml -n msdataworkshop
