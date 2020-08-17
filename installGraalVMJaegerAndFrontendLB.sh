#!/bin/bash

echo download and extract graalvm...
curl -sLO https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.1.0/graalvm-ce-java11-linux-amd64-20.1.0.tar.gz
gunzip graalvm-ce-java11-linux-amd64-20.1.0.tar.gz
tar xvf graalvm-ce-java11-linux-amd64-20.1.0.tar
rm graalvm-ce-java11-linux-amd64-20.1.0.tar
mv graalvm-ce-java11-20.1.0 ~/

echo install jaeger...
kubectl create -f https://tinyurl.com/yxn83h3q -n msdataworkshop

echo create frontend LB...
kubectl create -f frontend-helidon/frontend-service.yaml -n msdataworkshop
