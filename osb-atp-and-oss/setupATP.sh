#!/bin/bash

########################################################################################
# MODIFY "< >" VALUES IN EXPORTS BELOW....
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

#kubectl delete -f atp-existing-instance-order.yaml
#kubectl create -f atp-existing-instance-order.yaml
#kubectl delete -f atp-existing-instance-inventory.yaml
#kubectl create -f atp-existing-instance-inventory.yaml

kubectl delete secret atp-demo-binding-order -n msdataworkshop
kubectl delete -f atp-binding-plain-order.yaml
kubectl create -f atp-binding-plain-order.yaml
kubectl get secret atp-demo-binding-order --export -o yaml |  kubectl apply --namespace=msdataworkshop -f -
kubectl create -f atp-secret-orderuser.yaml -n msdataworkshop

#kubectl delete secret atp-demo-binding-inventory -n msdataworkshop
#kubectl delete -f atp-binding-plain-inventory.yaml
#kubectl create -f atp-binding-plain-inventory.yaml
#kubectl get secret atp-demo-binding-inventory --export -o yaml |  kubectl apply --namespace=msdataworkshop -f -
kubectl create -f atp-secret-inventoryuser.yaml -n msdataworkshop
