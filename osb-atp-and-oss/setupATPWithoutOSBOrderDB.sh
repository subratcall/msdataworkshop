#!/bin/bash

########################################################################################
# MODIFY "< >" VALUES IN EXPORTS BELOW....
########################################################################################

echo "order exports..."
echo "get wallet for order db..."
mkdir orderdbwallet
cd orderdbwallet
#oci db autonomous-database generate-wallet --autonomous-database-id <orderPDB_OCID> --file orderdbwallet/orderdbwallet.zip --password <orderPDB_WALLET_PW>
oci db autonomous-database generate-wallet --autonomous-database-id ocid1.autonomousdatabase.oc1.phx.abyhqljsal723ppfyoyd62esbe745hlkmwidrpz3eop57yyqc4q5t7tyw6ia --file orderdbwallet.zip --password Welcome_123
unzip orderdbwallet.zip
echo "for order admin and orderuser secrets..."
export cwallet_sso=$(cat cwallet.sso | base64 -w 0)
export ewallet_p12=$(cat ewallet.p12 | base64 -w 0)
export keystore_jks=$(cat keystore.jks | base64 -w 0)
export ojdbc_properties=$(cat ojdbc.properties | base64 -w 0)
export README=$(cat README | base64 -w 0)
export sqlnet_ora=$(cat sqlnet.ora | base64 -w 0)
#export tnsnames_ora=$(cat tnsnames.ora | base64 -w 0)
export tnsnames_ora=$(cat tnsnames.ora | base64 | tr -d '\n\r')
export truststore_jks=$(cat truststore.jks | base64 -w 0)
rm orderdbwallet.zip
cd ../


########################################################################################
# END MODIFY VALUES IN EXPORTS DO NOT ALTER FROM HERE TO END....
########################################################################################

export SCRIPT_DIR=$(dirname $0)

echo "atp-binding-order.yaml file replacements..."
eval "cat <<EOF
$(<$SCRIPT_DIR/atp-binding-order.yaml)
EOF" > $SCRIPT_DIR/atp-binding-order.yaml

#echo "..."
#echo "creating atp-binding-order.yaml..."
#kubectl create -f atp-binding-order.yaml -n msdataworkshop

#echo "..."
#kubectl get secrets -n msdataworkshop |grep atp
