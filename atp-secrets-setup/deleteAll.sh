#!/bin/bash

echo deleting all secrets in msdataworkshop namespace
kubectl delete --all secrets --namespace=msdataworkshop
