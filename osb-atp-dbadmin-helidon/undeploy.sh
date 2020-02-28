#!/bin/bash

kubectl delete -f helidon-mp-atp-service.yaml -n datademo
kubectl delete -f helidon-mp-atp-deployment.yaml -n datademo
