#!/bin/bash

echo delete order deployment and service...

kubectl delete deployment order-helidon -n msdataworkshop

kubectl delete service order -n msdataworkshop
