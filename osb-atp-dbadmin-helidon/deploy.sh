#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo create orderandinventory admin deployment and service...

if [ -z "$1" ]; then
    kubectl create -f $SCRIPT_DIR/atpadmin-orderandinventory-deployment.yaml -n msdataworkshop
else
    kubectl create -f <(istioctl kube-inject -f $SCRIPT_DIR/atpadmin-orderandinventory-deployment.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/orderandinventoryadmin-service.yaml -n msdataworkshop

