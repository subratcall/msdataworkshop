#!/bin/bash

kubectl delete -f helidon-mp-atp-service.yaml -n msdataworkshop
kubectl delete -f helidon-mp-atp-deployment.yaml -n msdataworkshop
