#!/bin/bash

source ../msdataworkshop.properties
export SCRIPT_DIR=$(dirname $0)
export CURRENTTIME=$( date '+%F_%H:%M:%S' )
echo CURRENTTIME is $CURRENTTIME  ...this will be appended to generated yamls
mkdir generated-yaml

echo "ORDER DB ......"
echo "get wallet for order db..."
mkdir orderdbwallet
cd orderdbwallet
echo "oci db autonomous-database generate-wallet --autonomous-database-id $ORDERPDB_OCID --file orderdbwallet.zip --password $orderpdb_walletPassword"
oci db autonomous-database generate-wallet --generate-type all --autonomous-database-id $ORDERPDB_OCID --file orderdbwallet.zip --password $orderpdb_walletPassword
unzip orderdbwallet.zip
rm orderdbwallet.zip
echo "export values for contents of wallet zip..."
export orderpdb_cwallet_sso=$(cat cwallet.sso | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export orderpdb_ewallet_p12=$(cat ewallet.p12 | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export orderpdb_keystore_jks=$(cat keystore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export orderpdb_ojdbc_properties=$(cat ojdbc.properties | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export orderpdb_README=$(cat README | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export orderpdb_sqlnet_ora=$(cat sqlnet.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export orderpdb_tnsnames_ora=$(cat tnsnames.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export orderpdb_truststore_jks=$(cat truststore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
cd ../

echo "base64 pws..."
export orderpdb_walletPassword=$(echo $orderpdb_walletPassword | base64)
export orderpdb_admin_password=$(echo $orderpdb_admin_password | base64)
export orderpdb_orderuser_password=$(echo $orderpdb_orderuser_password | base64)

echo "replace values in order yaml files (files are suffixed with ${CURRENTTIME})..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-binding-order.yaml)
EOF" > $SCRIPT_DIR/generated-yaml/atp-binding-order-${CURRENTTIME}.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-order-admin.yaml)
EOF" > $SCRIPT_DIR/generated-yaml/atp-secret-order-admin-${CURRENTTIME}.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-orderuser.yaml)
EOF" > $SCRIPT_DIR/generated-yaml/atp-secret-orderuser-${CURRENTTIME}.yaml

echo "creating order binding, and admin and orderuser secrets..."
kubectl create -f generated-yaml/atp-binding-order-${CURRENTTIME}.yaml -n msdataworkshop
kubectl create -f generated-yaml/atp-secret-order-admin-${CURRENTTIME}.yaml -n msdataworkshop
kubectl create -f generated-yaml/atp-secret-orderuser-${CURRENTTIME}.yaml -n msdataworkshop



echo "INVENTORY DB ......"
echo "get wallet for inventory db..."
mkdir inventorydbwallet
cd inventorydbwallet
echo "oci db autonomous-database generate-wallet --autonomous-database-id $INVENTORYPDB_OCID --file dbwallet.zip --password $inventorypdb_walletPassword"
oci db autonomous-database generate-wallet --autonomous-database-id $INVENTORYPDB_OCID --file inventorydbwallet.zip --password $inventorypdb_walletPassword
unzip inventorydbwallet.zip
rm inventorydbwallet.zip
export inventorypdb_cwallet_sso=$(cat cwallet.sso | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export inventorypdb_ewallet_p12=$(cat ewallet.p12 | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export inventorypdb_keystore_jks=$(cat keystore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export inventorypdb_ojdbc_properties=$(cat ojdbc.properties | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export inventorypdb_README=$(cat README | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export inventorypdb_sqlnet_ora=$(cat sqlnet.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export inventorypdb_tnsnames_ora=$(cat tnsnames.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export inventorypdb_truststore_jks=$(cat truststore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
cd ../

echo "base64 pws..."
export inventorypdb_walletPassword=$(echo $inventorypdb_walletPassword | base64)
export inventorypdb_admin_password=$(echo $inventorypdb_admin_password | base64)
export inventorypdb_inventoryuser_password=$(echo $inventorypdb_inventoryuser_password | base64)

echo "replace values in inventory yaml files (files are suffixed with ${CURRENTTIME})..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-binding-inventory.yaml)
EOF" > $SCRIPT_DIR/generated-yaml/atp-binding-inventory-${CURRENTTIME}.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventory-admin.yaml)
EOF" > $SCRIPT_DIR/generated-yaml/atp-secret-inventory-admin-${CURRENTTIME}.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventoryuser.yaml)
EOF" > $SCRIPT_DIR/generated-yaml/atp-secret-inventoryuser-${CURRENTTIME}.yaml

echo "creating inventory binding, and admin and inventoryuser secrets..."
kubectl create -f generated-yaml/atp-binding-inventory-${CURRENTTIME}.yaml -n msdataworkshop
kubectl create -f generated-yaml/atp-secret-inventory-admin-${CURRENTTIME}.yaml -n msdataworkshop
kubectl create -f generated-yaml/atp-secret-inventoryuser-${CURRENTTIME}.yaml -n msdataworkshop