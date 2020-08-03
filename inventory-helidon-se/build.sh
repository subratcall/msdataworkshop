#!/bin/bash
DOCKER_REGISTRY="iad.ocir.io/maacloud/msdataworkshop"
IMAGE_NAME="inventory-helidon-se"
IMAGE_VERSION="0.1"

export DOCKER_REGISTRY="$DOCKER_REGISTRY"
export IMAGE_NAME="$IMAGE_NAME"
export IMAGE_VERSION="$IMAGE_VERSION"

if [ -z "$DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

mvn install
mvn package docker:build

docker push "$IMAGE"
if [  $? -eq 0 ]; then
    docker rmi "$IMAGE"
fi