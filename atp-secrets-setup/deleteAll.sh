#!/bin/bash

echo deleting all secrets in msdataworkshop namespace...
kubectl delete --all secrets --namespace=msdataworkshop

echo deleting generated-yaml dir...
rm -rf generated-yaml

echo deleting wallet dirs...
rm -rf orderdbwallet
rm -rf inventorydbwallet