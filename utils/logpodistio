#!/bin/bash
export k8s_pod=$1
# export k8s_container=$2
for line in $(kubectl get pods --all-namespaces | \
  grep $k8s_pod | awk '{print $2}'); do
    echo "kubectl logs -f $line -n msdataworkshop $k8s_pod"
    kubectl logs -f $line -n msdataworkshop $k8s_pod
done
