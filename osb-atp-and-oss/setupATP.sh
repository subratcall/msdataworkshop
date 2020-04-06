#!/bin/bash

########################################################################################
# MODIFY "< >" VALUES IN EXPORTS BELOW....
########################################################################################

echo "order exports..."
echo "for the atp ServiceInstance (existing instance)..."
export orderpdb_ocid=<ORDERPDB_OCID>
export orderpdb_compartmentId=<ORDERPDB_COMPARTENT_OCID>
echo "for order admin and orderuser secrets..."
export orderpdb_walletPassword=$(echo <ORDERPDB_WALLET_PW> | base64)
export orderpdb_admin_password=$(echo <ORDERPDB_ADMIN_PW> | base64)
export orderpdb_orderuser_password=$(echo <ORDERPDB_ORDERUSER_PW> | base64)

echo "inventory exports..."
echo "for the atp ServiceInstance (existing instance)..."
export inventorypdb_ocid=<INVENTORYPDB_OCID>
export inventorypdb_compartmentId=<INVENTORYPDB_COMPARTENT_OCID>
echo "for inventory admin and inventoryuser secrets..."
export inventorypdb_walletPassword=$(echo <INVENTORYPDB_WALLET_PW> | base64)
export inventorypdb_admin_password=$(echo <INVENTORYPDB_ADMIN_PW> | base64)
export inventorypdb_inventoryuser_password=$(echo <INVENTORYPDB_INVENTORYUSER_PW> | base64)


########################################################################################
# END MODIFY VALUES IN EXPORTS DO NOT ALTER FROM HERE TO END....
########################################################################################

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

echo "creating order and inventory instances..."
kubectl delete -f atp-existing-instance-order.yaml
kubectl create -f atp-existing-instance-order.yaml
kubectl delete -f atp-existing-instance-inventory.yaml
kubectl create -f atp-existing-instance-inventory.yaml

echo "creating order binding and secrets..."
kubectl delete secret atp-demo-binding-order -n msdataworkshop
kubectl delete -f atp-binding-plain-order.yaml
kubectl create -f atp-binding-plain-order.yaml
kubectl get secret atp-demo-binding-order --export -o yaml |  kubectl apply --namespace=msdataworkshop -f -
kubectl create -f atp-secret-order-admin.yaml -n msdataworkshop
kubectl create -f atp-secret-orderuser.yaml -n msdataworkshop

echo "creating inventory binding and secrets..."
kubectl delete secret atp-demo-binding-inventory -n msdataworkshop
kubectl delete -f atp-binding-plain-inventory.yaml
kubectl create -f atp-binding-plain-inventory.yaml
kubectl get secret atp-demo-binding-inventory --export -o yaml |  kubectl apply --namespace=msdataworkshop -f -
kubectl create -f atp-secret-inventory-admin.yaml -n msdataworkshop
kubectl create -f atp-secret-inventoryuser.yaml -n msdataworkshop
