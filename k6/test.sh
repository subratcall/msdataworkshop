export VUS="$1"
export DURATION="20"

set -e

if [[ ! $LB =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    echo "Set variable LB to the external IP address of the ext-order service"
    exit
fi

NEXT_RUN_FILE=next_run

if [ ! -f "$NEXT_RUN_FILE" ]; then
    echo 0 > $NEXT_RUN_FILE
fi

export RUN=`cat $NEXT_RUN_FILE`
RUN=$((RUN+1))
echo $RUN > $NEXT_RUN_FILE

mkdir "RUN${RUN}"

./k6 run --vus $VUS --duration "${DURATION}s" --address localhost:6566 placeorder.js 2>&1 | tee "RUN${RUN}"/placeorder.log &
# sleep 1
# ./k6 run --vus $VUS --duration "${DURATION}s" showorder.js 2>&1 | tee "RUN${RUN}"/showorder.log &
wait
