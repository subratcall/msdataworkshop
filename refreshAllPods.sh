#!/bin/bash

echo building all images and deleting all pods to refresh...

echo building all images...
./build.sh

echo deleting all podss...
./deleteAllPods.sh

