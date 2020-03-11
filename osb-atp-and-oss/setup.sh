#!/bin/bash

echo "Install svc"
# linux is currently assumed, check
curl -sLO https://download.svcat.sh/cli/latest/linux/amd64/svcat
chmod +x ./svcat
export PATH=$PATH:./svcat
svcat version --client

echo "Add the Kubernetes Service Catalog helm repository:"
helm repo add svc-cat https://svc-catalog-charts.storage.googleapis.com

echo "Install the Kubernetes Service Catalog helm chart:"
# detect helm and if Helm 3.x ... helm install catalog svc-cat/catalog
helm install svc-cat/catalog --timeout 300 --name catalog

########################################################################################
# MODIFY "< >" VALUES IN EXPORTS AND ocicredentials ARGUMENTS BELOW....
########################################################################################

echo "order exports..."
echo "for the atp ServiceInstance (existing instance)..."
export orderpdb_ocid=<ORDERPDB_OCID>
export orderpdb_compartmentId=<ORDERPDB_COMPARTENT_OCID>
echo "for order admin and orderuser secrets..."
export orderpdb_walletPassword=<ORDERPDB_WALLET_PW>
export orderpdb_admin_password=<ORDERPDB_ADMIN_PW>
export orderpdb_orderuser_password=<ORDERPDB_ORDERUSER_PW>

echo "inventory exports..."
echo "for the atp ServiceInstance (existing instance)..."
export inventorypdb_ocid=<INVENTORYPDB_OCID>
export inventorypdb_compartmentId=<INVENTORYPDB_COMPARTENT_OCID>
echo "for inventory admin and inventoryuser secrets..."
export inventorypdb_walletPassword="<INVENTORYPDB_WALLET_PW>
export inventorypdb_admin_password=<INVENTORYPDB_ADMIN_PW>
export inventorypdb_inventoryuser_password=<INVENTORYPDB_INVENTORYUSER_PW>

kubectl create secret generic ocicredentials \
--from-literal=tenancy=<CUSTOMER_TENANCY_OCID> \
--from-literal=user=<USER_OCID> \
--from-literal=fingerprint=<USER_PUBLIC_API_KEY_FINGERPRINT> \
--from-literal=region=<USER_OCI_REGION> \
--from-literal=passphrase=<PASSPHRASE_STRING> \
--from-file=privatekey=<PATH_OF_USER_PRIVATE_API_KEY>


########################################################################################
# END MODIFY VALUES IN EXPORTS AND ocicredentials ARGUMENTS DO NOT ALTER FROM HERE TO END....
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

echo "sleep for 2 minutes and check again..."
sleep 120
svcat get brokers
svcat get classes
svcat get plans

export SCRIPT_DIR=$(dirname $0)

echo "order OSB yaml file replacments..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-existing-instance-order.yaml)
EOF" > $SCRIPT_DIR/atp-existing-instance-order.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-order-admin.yaml)
EOF" > $SCRIPT_DIR/atp-secret-order-admin.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-orderuser.yaml)
EOF" > $SCRIPT_DIR/atp-secret-orderuser.yaml

echo "inventory OSB yaml file replacments..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-existing-instance-inventory.yaml)
EOF" > $SCRIPT_DIR/atp-existing-instance-inventory.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventory-admin.yaml)
EOF" > $SCRIPT_DIR/atp-secret-inventory-admin.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventoryuser.yaml)
EOF" > $SCRIPT_DIR/atp-secret-inventoryuser.yaml

kubectl delete -f atp-existing-instance-order.yaml
kubectl create -f atp-existing-instance-order.yaml
kubectl delete -f atp-existing-instance-inventory.yaml
kubectl create -f atp-existing-instance-inventory.yaml

kubectl delete secret atp-demo-binding-order -n msdataworkshop
kubectl delete -f atp-binding-plain-order.yaml
kubectl create -f atp-binding-plain-order.yaml
kubectl get secret atp-demo-binding-order --export -o yaml |  kubectl apply --namespace=msdataworkshop -f -

kubectl delete secret atp-demo-binding-inventory -n msdataworkshop
kubectl delete -f atp-binding-plain-inventory.yaml
kubectl create -f atp-binding-plain-inventory.yaml
kubectl get secret atp-demo-binding-inventory --export -o yaml |  kubectl apply --namespace=msdataworkshop -f -
