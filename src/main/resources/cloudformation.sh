#!/usr/bin/env bash
export AWS_DEFAULT_REGION=$1
set -x
set -e

STACK_NAME=${2}
TEMPLATE_FILE=$3
if [ "$4" != "" ]; then
    PARAMETERS="--parameters file://$4"
else
    PARAMETERS=""

fi


function stackExists() {
    stackName=${1}
    set +e
    aws cloudformation describe-stacks --stack-name ${stackName}
    found=(${?} eq '0')
    set -e
    return ${found};
}

UPDATE_LOG=/tmp/cf-update-${STACK_NAME}.log

if ! stackExists ${STACK_NAME}; then
    aws cloudformation create-stack --capabilities CAPABILITY_NAMED_IAM --stack-name ${STACK_NAME} --template-body file://${TEMPLATE_FILE} ${PARAMETERS}
    aws cloudformation wait stack-create-complete --stack-name ${STACK_NAME}
else
    set +e
    aws cloudformation update-stack --capabilities CAPABILITY_NAMED_IAM --stack-name ${STACK_NAME} --template-body file://${TEMPLATE_FILE} ${PARAMETERS} > ${UPDATE_LOG} 2>&1
    updateCode=$?
    set -e
    if [ "${updateCode}" -eq "255" ] ; then
        cat ${UPDATE_LOG}
        rm -f ${UPDATE_LOG}
    else
        UPDATE_LOG=/tmp/update.cfm.log
        aws cloudformation wait stack-update-complete --stack-name ${STACK_NAME}
    fi
fi