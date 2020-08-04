#!/bin/bash
SCRIPT_DIR=$(dirname $0)
echo create atpaqadmin deployment and service...
export CURRENTTIME=$( date '+%F_%H:%M:%S' )
echo CURRENTTIME is $CURRENTTIME  ...this will be appended to generated deployment yaml
cp supplier-helidon-se-deployment.yaml supplier-helidon-se-deployment-$CURRENTTIME.yaml
#may hit sed incompat issue with mac
sed -i "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" supplier-helidon-se-deployment-$CURRENTTIME.yaml
sed -i "s|%INVENTORY_PDB_NAME%|${INVENTORY_PDB_NAME}|g" supplier-helidon-se-deployment-$CURRENTTIME.yaml
if [ -z "$1" ]; then
    kubectl apply -f $SCRIPT_DIR/supplier-helidon-se-deployment-$CURRENTTIME.yaml -n msdataworkshop
else
    kubectl apply -f <(istioctl kube-inject -f $SCRIPT_DIR/supplier-helidon-se-deployment-$CURRENTTIME.yaml ) -n msdataworkshop
fi
kubectl create -f $SCRIPT_DIR/supplier-helidon-se-service.yaml  -n msdataworkshop