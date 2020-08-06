#!/bin/bash

echo delete inventory-nodejs deployment...

kubectl delete deployment inventory-nodejs -n msdataworkshop

