#!/bin/bash

echo delete frontend deployment and service...

kubectl delete deployment frontend-helidon -n msdataworkshop

kubectl delete service frontend -n msdataworkshop