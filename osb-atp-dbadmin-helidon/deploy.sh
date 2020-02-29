#!/bin/bash

kubectl create -f helidon-mp-atp-deploymentyaml

kubectl create -f helidon-mp-atp-service.yaml
