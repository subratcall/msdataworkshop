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
export cwallet_sso=$(cat cwallet.sso | base64)
export ewallet_p12=$(cat ewallet.p12 | base64)
export keystore_jks=$(cat keystore.jks | base64)
export ojdbc_properties=$(cat ojdbc.properties | base64)
export README=$(cat README | base64)
export sqlnet_ora=$(cat sqlnet.ora | base64)
export tnsnames_ora=$(cat tnsnames.ora | base64)
export truststore_jks=$(cat truststore.jks | base64)
rm inventorydbwallet.zip
cd ../

########################################################################################
# END MODIFY VALUES IN EXPORTS DO NOT ALTER FROM HERE TO END....
########################################################################################

export SCRIPT_DIR=$(dirname $0)

echo "inventory yaml file replacements..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-binding-inventory.yaml)
EOF" > $SCRIPT_DIR/atp-binding-inventory.yaml

echo "..."
echo "creating inventory binding and secrets..."
kubectl create -f atp-binding-inventory.yaml -n msdataworkshop

echo "..."
#svcat get bindings
kubectl get secrets -n msdataworkshop |grep atp
