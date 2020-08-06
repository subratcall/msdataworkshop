#!/bin/bash

echo delete supplier deployment and service...

kubectl delete deployment supplier-helidon-se -n msdataworkshop

kubectl delete service supplier -n msdataworkshop
