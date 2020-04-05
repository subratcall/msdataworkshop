#!/bin/bash

echo "Uninstall OSB"

kubectl delete -f oci-service-broker.yaml
helm uninstall oci-service-broker
kubectl delete secret ocicredentials
kubectl delete clusterrolebinding cluster-admin-brokers
helm uninstall catalog
helm repo remove svc-cat
rm $MSDATAWORKSHOP_LOCATION/utils/svcat

echo "Uninstall OSB complete"
