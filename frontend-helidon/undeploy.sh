#!/bin/bash

echo delete frontend deployment and service...

kubectl delete deployment inventory-helidon-se -n msdataworkshop

kubectl delete service inventory-helidon-se -n msdataworkshop
