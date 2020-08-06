#!/bin/bash

echo delete inventory-helidon deployment...

kubectl delete deployment inventory-helidon -n msdataworkshop
