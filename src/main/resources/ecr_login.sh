#!/usr/bin/env bash

set -x
ECR_ACCOUNT_ID=$1
REGION=$2
PROJECT_NAME=$3
BUILD_VERSION=$4
PROFILE=$5
if [ "${PROFILE}" != "" ]; then
    export AWS_PROFILE=${PROFILE}
fi

REGISTRY_ID=${ECR_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com
DOCKER_REPO=${REGISTRY_ID}/${PROJECT_NAME}

if [ "${REGION}" != "" ] ; then
     $(aws ecr get-login --region ${REGION} --no-include-email)
else
    $(aws ecr get-login --no-include-email)
fi

if ! aws ecr describe-repositories --region ${REGION} --repository-names ${PROJECT_NAME}; then
        # create repository
        aws ecr create-repository --region ${REGION} --repository-name ${PROJECT_NAME}
fi
docker build --pull -t ${DOCKER_REPO}:latest -t ${DOCKER_REPO}:${BUILD_VERSION} --label version=${BUILD_VERSION} --label name=${PROJECT_NAME} .
docker push ${DOCKER_REPO}