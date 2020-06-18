#!/bin/bash

SCRIPT_DIR=$(dirname $0)

IMAGE_NAME=inventory-python
IMAGE_VERSION=0.1

if [ -z "DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

docker build -t $IMAGE .

docker push $IMAGE
