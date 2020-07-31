#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo create order-helidon deployment and service...
export CURRENTTIME=$( date '+%F_%H:%M:%S' )
echo CURRENTTIME is $CURRENTTIME  ...this will be appended to generated deployment yaml

cp order-helidon-deployment.yaml order-helidon-deployment-$CURRENTTIME.yaml

#may hit sed incompat issue with mac
sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" order-helidon-deployment-$CURRENTTIME.yaml
sed -i "s|%ORDER_PDB_NAME%|${ORDER_PDB_NAME}|g" order-helidon-deployment-${CURRENTTIME}.yaml

if [ -z "$1" ]; then
    kubectl apply -f $SCRIPT_DIR/order-helidon-deployment-$CURRENTTIME.yaml -n msdataworkshop
else
    kubectl apply -f <(istioctl kube-inject -f $SCRIPT_DIR/order-helidon-deployment-$CURRENTTIME.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/order-service.yaml -n msdataworkshop
