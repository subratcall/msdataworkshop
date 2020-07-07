#!/bin/bash

########################################################################################
# MODIFY "< >" VALUES IN EXPORTS BELOW....
########################################################################################

echo "inventory exports..."
echo "get wallet for inventory db..."
mkdir inventorydbwallet
cd inventorydbwallet
#oci db autonomous-database generate-wallet --autonomous-database-id <INVENTORYPDB_OCID> --file inventorydbwallet/inventorydbwallet.zip --password <INVENTORYPDB_WALLET_PW>
oci db autonomous-database generate-wallet --autonomous-database-id ocid1.autonomousdatabase.oc1.phx.abyhqljsykgg4c5ou2yllx6pkt76nxppmt3wbmx2hwztkxkgmpjatz6fsxqq --file inventorydbwallet.zip --password Welcome_123
unzip inventorydbwallet.zip
echo "for inventory admin and inventoryuser secrets..."
export cwallet_sso=$(cat cwallet.sso | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export ewallet_p12=$(cat ewallet.p12 | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export keystore_jks=$(cat keystore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export ojdbc_properties=$(cat ojdbc.properties | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export README=$(cat README | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export sqlnet_ora=$(cat sqlnet.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
#export tnsnames_ora=$(cat tnsnames.ora | base64 -w 0)
export tnsnames_ora=$(cat tnsnames.ora | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
export truststore_jks=$(cat truststore.jks | base64 | tr -d '\n\r' | base64 | tr -d '\n\r')
rm inventorydbwallet.zip
cd ../

########################################################################################
# END MODIFY VALUES IN EXPORTS DO NOT ALTER FROM HERE TO END....
########################################################################################

export SCRIPT_DIR=$(dirname $0)

echo "atp-binding-inventory.yaml file replacements..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-binding-inventory.yaml)
EOF" > $SCRIPT_DIR/atp-binding-inventory.yaml

echo "..."
echo "creating atp-binding-inventory.yaml..."
kubectl create -f atp-binding-inventory.yaml -n msdataworkshop

echo "..."
kubectl get secrets -n msdataworkshop |grep atp
