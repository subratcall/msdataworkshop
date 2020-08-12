export RUN="$1"
export VUS="$2"
export DURATION="5"

set -e

case "$RUN" in
    ''|*[!0-9]*) 
        echo "Run number must be a positive integer"
        exit
    ;;
esac

mkdir "RUN${RUN}"

./k6 run --vus $VUS --duration "${DURATION}s" --address localhost:6566 placeorder.js 2>&1 | tee "RUN${RUN}"/placeorder.log &
# sleep 1
# ./k6 run --vus $VUS --duration "${DURATION}s" showorder.js 2>&1 | tee "RUN${RUN}"/showorder.log &
wait
