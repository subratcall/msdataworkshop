#!/bin/bash
export k8s_pod=$1
for line in $(kubectl get pods --all-namespaces | grep $k8s_pod | awk '{print $2}'); do
    echo "kubectl port-forward $line -n msdataworkshop 8080:8080 "
    kubectl port-forward $line -n msdataworkshop 8080:8080
done
