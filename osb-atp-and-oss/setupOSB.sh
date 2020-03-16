#!/bin/bash

echo "Install svcat"
# linux is currently assumed, check
curl -sLO https://download.svcat.sh/cli/latest/linux/amd64/svcat
chmod +x ./svcat
echo "moving svcat to `utils` dir to add it to path..."
mv svcat ../utils
svcat version --client

echo "Add the Kubernetes Service Catalog helm repository:"
helm repo add svc-cat https://svc-catalog-charts.storage.googleapis.com

echo "Install the Kubernetes Service Catalog helm chart:"
# detect helm and if Helm 3.x ... helm install catalog svc-cat/catalog
helm install svc-cat/catalog --timeout 300 --name catalog

########################################################################################
# MODIFY "< >" VALUES IN clusterrolebinding and ocicredentials ARGUMENTS BELOW....
########################################################################################

# Note that `--user=<USER_ID>` is id not ocid, for example, `--user=paul.parkinson`
kubectl create clusterrolebinding cluster-admin-brokers --clusterrole=cluster-admin --user=<USER_ID>

# If not already created, create user API Key with password in order to obtain fingerprint, etc.
#    as described here: https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
kubectl create secret generic ocicredentials \
--from-literal=tenancy=<CUSTOMER_TENANCY_OCID> \
--from-literal=user=<USER_OCID> \
--from-literal=fingerprint=<USER_PUBLIC_API_KEY_FINGERPRINT> \
--from-literal=region=<USER_OCI_REGION> \
--from-literal=passphrase=<PASSPHRASE_STRING> \
--from-file=privatekey=<PATH_OF_USER_PRIVATE_API_KEY>

########################################################################################
# END MODIFY VALUES IN clusterrolebinding and ocicredentials DO NOT ALTER FROM HERE TO END....
########################################################################################

echo "install oci-service-broker:"
# detect helm and if Helm 3.x ...
# helm install oci-service-broker https://github.com/oracle/oci-service-broker/releases/download/v1.3.3/oci-service-broker-1.3.3.tgz \
#   --set ociCredentials.secretName=ocicredentials \
#   --set storage.etcd.useEmbedded=true \
#   --set tls.enabled=false
helm install https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz  --name oci-service-broker \
  --set ociCredentials.secretName=ocicredentials \
  --set storage.etcd.useEmbedded=true \
  --set tls.enabled=false

echo "create oci-service-broker ClusterServiceBroker:"
kubectl create -f oci-service-broker.yaml

echo " svcat get brokers..."
svcat get brokers

echo "sleep for 1 minute and check again..."
sleep 60
svcat get brokers
svcat get classes
svcat get plans

echo "if still not ready continue to  check again with 'svcat get brokers ; svcat get classes ; svcat get plans''