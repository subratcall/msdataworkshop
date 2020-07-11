#!/bin/bash

# either the following command or console can be used to find the DB ocids...
# oci db autonomous-database list --compartment-id <COMPARTMENT_OCID>
# oci db autonomous-database list --compartment-id ocid1.compartment.oc1..aaaaaaaatvh4oetwxoay4u6lj64mg7n6bvbc63wmesbwyfsvjlpp5zqhi3sa

export SCRIPT_DIR=$(dirname $0)
echo "ORDER DB ......"
########################################################################################
# MODIFY "< >" VALUES for ORDER DB....
########################################################################################
export orderpdb_walletPassword=$(echo <ORDERPDB_WALLET_PW> | base64)
export orderpdb_admin_password=$(echo <ORDERPDB_ADMIN_PW> | base64)
export orderpdb_orderuser_password=$(echo <ORDERPDB_ORDERUSER_PW> | base64)
echo "get wallet for order db..."
mkdir orderdbwallet
cd orderdbwallet
oci db autonomous-database generate-wallet --autonomous-database-id <ORDERPDB_OCID> --file orderdbwallet.zip --password <ORDERPDB_WALLET_PW>
#example... oci db autonomous-database generate-wallet --autonomous-database-id ocid1.autonomousdatabase.oc1.phx.abyhqljsykgg4c5ou2yllx6pkt76nxppmt3wbmx2hwztkxkgmpjatz6fsxqq --file orderdbwallet.zip --password Welcome_123
########################################################################################
# END MODIFY "< >" VALUES for ORDER DB....
########################################################################################
unzip orderdbwallet.zip
export cwallet_sso=$(cat cwallet.sso | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export ewallet_p12=$(cat ewallet.p12 | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export keystore_jks=$(cat keystore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export ojdbc_properties=$(cat ojdbc.properties | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export README=$(cat README | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export sqlnet_ora=$(cat sqlnet.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export tnsnames_ora=$(cat tnsnames.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export truststore_jks=$(cat truststore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
rm orderdbwallet.zip
cd ../
rm -rf orderdbwallet

echo "order yaml file replacements..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-binding-ordeer.yaml)
EOF" > $SCRIPT_DIR/atp-binding-order.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-order-admin.yaml)
EOF" > $SCRIPT_DIR/atp-secret-order-admin.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-orderuser.yaml)
EOF" > $SCRIPT_DIR/atp-secret-orderuser.yaml

echo "creating order binding, and admin and orderuser secrets..."
kubectl create -f atp-binding-order.yaml -n msdataworkshop
kubectl create -f atp-secret-order-admin.yaml -n msdataworkshop
kubectl create -f atp-secret-orderuser.yaml -n msdataworkshop




echo "INVENTORY DB ......"
########################################################################################
# MODIFY "< >" VALUES for INVENTORY DB....
########################################################################################
export inventorypdb_walletPassword=$(echo <INVENTORYPDB_WALLET_PW> | base64)
export inventorypdb_admin_password=$(echo <INVENTORYPDB_ADMIN_PW> | base64)
export inventorypdb_inventoryuser_password=$(echo <INVENTORYPDB_INVENTORYUSER_PW> | base64)
echo "get wallet for inventory db..."
mkdir inventorydbwallet
cd inventorydbwallet
oci db autonomous-database generate-wallet --autonomous-database-id <INVENTORYPDB_OCID> --file dbwallet.zip --password <INVENTORYPDB_WALLET_PW>
#example... oci db autonomous-database generate-wallet --autonomous-database-id ocid1.autonomousdatabase.oc1.phx.abyhqljsykgg4c5ou2yllx6pkt76nxppmt3wbmx2hwztkxkgmpjatz6fsxqq --file inventorydbwallet.zip --password Welcome_123
########################################################################################
# END MODIFY "< >" VALUES for INVENTORY DB....
########################################################################################
unzip inventorydbwallet.zip
export cwallet_sso=$(cat cwallet.sso | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export ewallet_p12=$(cat ewallet.p12 | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export keystore_jks=$(cat keystore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export ojdbc_properties=$(cat ojdbc.properties | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export README=$(cat README | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export sqlnet_ora=$(cat sqlnet.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export tnsnames_ora=$(cat tnsnames.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export truststore_jks=$(cat truststore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
rm inventorydbwallet.zip
cd ../
rm -rf inventorydbwallet

echo "inventory yaml file replacements..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-binding-ordeer.yaml)
EOF" > $SCRIPT_DIR/atp-binding-inventory.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventory-admin.yaml)
EOF" > $SCRIPT_DIR/atp-secret-inventory-admin.yaml
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-secret-inventoryuser.yaml)
EOF" > $SCRIPT_DIR/atp-secret-inventoryuser.yaml

echo "creating inventory binding, and admin and inventoryuser secrets..."
kubectl create -f atp-binding-inventory.yaml -n msdataworkshop
kubectl create -f atp-secret-inventory-admin.yaml -n msdataworkshop
kubectl create -f atp-secret-inventoryuser.yaml -n msdataworkshop