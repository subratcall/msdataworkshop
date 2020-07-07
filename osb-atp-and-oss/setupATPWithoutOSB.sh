#!/bin/bash

########################################################################################
# MODIFY "< >" VALUES IN EXPORTS BELOW....
########################################################################################


echo "order exports..."
echo "get wallet for order db..."
mkdir orderdbwallet
oci db autonomous-database generate-wallet --autonomous-database-id <ORDERPDB_OCID> --file orderdbwallet/orderdbwallet.zip --password <ORDERPDB_WALLET_PW>
#oci db autonomous-database generate-wallet --autonomous-database-id ocid1.autonomousdatabase.oc1.phx.abyhqljsal723ppfyoyd62esbe745hlkmwidrpz3eop57yyqc4q5t7tyw6ia --file orderdbwallet/orderdbwallet.zip --password Welcome_123
cd orderdbwallet
unzip orderdbwallet.zip
rm orderdbwallet.zip
echo "for order admin and orderuser secrets..."
export orderpdb_walletPassword=$(echo <ORDERPDB_WALLET_PW> | base64)
export orderpdb_admin_password=$(echo <ORDERPDB_ADMIN_PW> | base64)
export orderpdb_orderuser_password=$(echo <ORDERPDB_ORDERUSER_PW> | base64)
export orderpd_cwallet_sso=$(cat cwallet.sso | base64)

echo "inventory exports..."
echo "get wallet for inventory db..."
mkdir inventorydbwallet
oci db autonomous-database generate-wallet --autonomous-database-id <INVENTORYPDB_OCID> --file inventorydbwallet/inventorydbwallet.zip --password <INVENTORYPDB_WALLET_PW>
#oci db autonomous-database generate-wallet --autonomous-database-id ocid1.autonomousdatabase.oc1.phx.abyhqljsykgg4c5ou2yllx6pkt76nxppmt3wbmx2hwztkxkgmpjatz6fsxqq --file inventorydbwallet/inventorydbwallet.zip --password Welcome_123
cd inventorydbwallet
unzip inventorydbwallet.zip
rm inventorydbwallet.zip
echo "for inventory admin and inventoryuser secrets..."
export inventorypdb_walletPassword=$(echo <INVENTORYPDB_WALLET_PW> | base64)
export inventorypdb_admin_password=$(echo <INVENTORYPDB_ADMIN_PW> | base64)
export inventorypdb_inventoryuser_password=$(echo <INVENTORYPDB_INVENTORYUSER_PW> | base64)




########################################################################################
# END MODIFY VALUES IN EXPORTS DO NOT ALTER FROM HERE TO END....
########################################################################################

export SCRIPT_DIR=$(dirname $0)

echo "order yaml file replacments..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-existing-instance-order.yaml)
EOF" > $SCRIPT_DIR/atp-binding-order.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-order-admin.yaml)
EOF" > $SCRIPT_DIR/atp-secret-order-admin.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-orderuser.yaml)
EOF" > $SCRIPT_DIR/atp-secret-orderuser.yaml

echo "inventory yaml file replacments..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-existing-instance-inventory.yaml)
EOF" > $SCRIPT_DIR/atp-binding-inventory.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventory-admin.yaml)
EOF" > $SCRIPT_DIR/atp-secret-inventory-admin.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventoryuser.yaml)
EOF" > $SCRIPT_DIR/atp-secret-inventoryuser.yaml

echo "note that NotFound errors from the following delete/cleanup commands are expected if this is first install..."
sleep 5
kubectl delete secret atp-user-cred-orderadmin  atp-user-cred-orderuser atp-demo-binding-inventory  -n msdataworkshop
kubectl delete secret atp-user-cred-inventoryadmin atp-user-cred-inventoryuser atp-demo-binding-order -n msdataworkshop

echo "..."
echo "creating order binding and secrets..."
kubectl create -f atp-binding-order.yaml -n msdataworkshop
kubectl create -f atp-secret-order-admin.yaml -n msdataworkshop
kubectl create -f atp-secret-orderuser.yaml -n msdataworkshop

echo "..."
echo "creating inventory binding and secrets..."
kubectl create -f atp-binding-inventory.yaml -n msdataworkshop
kubectl create -f atp-secret-inventory-admin.yaml -n msdataworkshop
kubectl create -f atp-secret-inventoryuser.yaml -n msdataworkshop

echo "..."
#svcat get bindings
kubectl get secrets -n msdataworkshop |grep atp
