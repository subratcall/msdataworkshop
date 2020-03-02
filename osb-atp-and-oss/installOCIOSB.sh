#!/bin/bash

helm install oci-service-broker https://github.com/oracle/oci-service-broker/releases/download/v1.3.3/oci-service-broker-1.3.3.tgz \
  --set ociCredentials.secretName=ocicredentials \
  --set storage.etcd.useEmbedded=true \
  --set tls.enabled=false
kubectl create -f oci-service-broker/samples/oci-service-broker.yaml