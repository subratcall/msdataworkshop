#!/bin/bash

echo delete atpaqadmin deployment and service...

kubectl delete deployment atpaqadmin -n msdataworkshop

kubectl delete service atpaqadmin -n msdataworkshop

