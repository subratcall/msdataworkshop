#!/bin/bash

echo "Install svcat"
curl -sLO https://download.svcat.sh/cli/latest/linux/amd64/svcat
chmod +x ./svcat
echo "moving svcat to utils dir to add it to path..."
mv svcat $MSDATAWORKSHOP_LOCATION/utils
svcat version --client

echo "Add the Kubernetes Service Catalog helm repository:"
helm repo add svc-cat https://svc-catalog-charts.storage.googleapis.com

echo "Install the Kubernetes Service Catalog helm chart:"
helm install catalog svc-cat/catalog

########################################################################################
# MODIFY "< >" VALUES IN clusterrolebinding and ocicredentials ARGUMENTS BELOW....
########################################################################################
# Note that `--user=<USER_ID>` is id not ocid, for example, `--user=paul.parkinson`
kubectl create clusterrolebinding cluster-admin-brokers --clusterrole=cluster-admin --user=paul.parkinson@gmail.com

# If not already created, create user API Key with password in order to obtain fingerprint, etc.
#    as described here: https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
kubectl create secret generic ocicredentials \
--from-literal=tenancy=ocid1.tenancy.oc1..aaaaaaaab6vzaylctdrkxvr2pa6h6nkn3b5y32wg4e572lubuy33na6d3jja \
--from-literal=user=ocid1.user.oc1..aaaaaaaaftpjzj5zod4wmmi3a44d2w5wnjibnk5uupe52hrgv5hcjab4ah2q \
--from-literal=fingerprint=07:a7:74:03:33:a0:0f:34:18:6a:3b:06:f8:39:59:80 \
--from-literal=region=us-phoenix-1 \
--from-literal=passphrase=foobar \
--from-file=privatekey=/home/paul_parki/.oci/paul_parkinson_privkey.pem

########################################################################################
# END MODIFY VALUES IN clusterrolebinding and ocicredentials DO NOT ALTER FROM HERE TO END....
########################################################################################

echo "Waiting for Service Catalog to be Running"
sleep 100
# this is to avoid "server is currently unable to handle the request" todo look into using kubectl wait --for=condition= Service Catalog

echo "install oci-service-broker:"
helm install oci-service-broker https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz \
   --set ociCredentials.secretName=ocicredentials \
   --set storage.etcd.useEmbedded=true \
   --set tls.enabled=false --wait

echo "create oci-service-broker ClusterServiceBroker:"
kubectl create -f oci-service-broker.yaml

echo "sleep for 1 minute and svcat get brokers... (initial check may temporarily show ErrorFetchingCatalog etc.)"
svcat get brokers

echo "sleep for 1 minute and check again..."
sleep 60
svcat get brokers
svcat get classes
svcat get plans

echo "If broker is still not ready, continue to check again with svcat get brokers etc commands"