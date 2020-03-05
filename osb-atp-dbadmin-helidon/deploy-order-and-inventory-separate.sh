#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo create orderadmin deployment and service...

if [ -z "$1" ]; then
    kubectl create -f $SCRIPT_DIR/atpadmin-order-deployment.yaml -n msdataworkshop
else
    kubectl create -f <(istioctl kube-inject -f $SCRIPT_DIR/atpadmin-order-deployment.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/orderadmin-service.yaml -n msdataworkshop


echo create inventoryadmin deployment and service...

if [ -z "$1" ]; then
    kubectl create -f $SCRIPT_DIR/atpadmin-inventory-deployment.yaml -n msdataworkshop
else
    kubectl create -f <(istioctl kube-inject -f $SCRIPT_DIR/atpadmin-inventory-deployment.yaml) -n msdataworkshop
fi

kubectl create -f $SCRIPT_DIR/inventoryadmin-service.yaml -n msdataworkshop