#!/bin/bash

SCRIPT_DIR=$(dirname $0)

echo delete atpaqadmin deployment and service...

kubectl delete deployment atpaqadmin -n msdataworkshop

kubectl delete -f $SCRIPT_DIR/atpaqadmin-service.yaml -n msdataworkshop

