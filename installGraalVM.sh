#!/bin/bash

echo download and extract graalvm...
curl -sLO https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.1.0/graalvm-ce-java11-linux-amd64-20.1.0.tar.gz
gunzip graalvm-ce-java11-linux-amd64-20.1.0.tar.gz
tar xvf graalvm-ce-java11-linux-amd64-20.1.0.tar
rm graalvm-ce-java11-linux-amd64-20.1.0.tar

