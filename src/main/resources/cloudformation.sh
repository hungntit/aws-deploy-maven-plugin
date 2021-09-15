#!/usr/bin/env bash
export AWS_DEFAULT_REGION=$1
set -x
set -e

STACK_NAME=${2}
CAPABILITIES=${3:-CAPABILITY_NAMED_IAM}
TEMPLATE_FILE=$4

if [ "$5" != "" ]; then
    PARAMETERS="--parameters file://${5}"
else
    PARAMETERS=""
fi

PREFIX=$6

if [ "$7" != "" ]; then
    TAGS_FILE="--tags file://${7}"
else
    TAGS_FILE=""
fi

CUSTOM_PARAMS=$8
STACK_LOG=/tmp/stack-status.log
function stackExists() {
    local stackName=${1}
    set +e
    aws cloudformation describe-stacks --stack-name ${stackName} > ${STACK_LOG} 2>&1
    found=(${?} eq '0')
    local status=`cat ${STACK_LOG} | grep StackStatus | cut -d '"' -f4`
    if [ "${status}" == "ROLLBACK_COMPLETE" ];then
        echo "Stack is in ROLLBACK_COMPLETE, deleting ..."
        aws cloudformation  delete-stack --stack-name ${stackName}
        aws cloudformation wait stack-delete-complete --stack-name ${stackName}
        found=1
    fi
    set -e

    return ${found};
}

UPDATE_LOG=/tmp/cf-update-${STACK_NAME}.log

if ! stackExists ${PREFIX}${STACK_NAME}; then
    aws cloudformation create-stack --capabilities ${CAPABILITIES} --stack-name ${PREFIX}${STACK_NAME} --template-body file://${TEMPLATE_FILE} ${PARAMETERS} ${TAGS_FILE} ${CUSTOM_PARAMS}
    aws cloudformation wait stack-create-complete --stack-name ${PREFIX}${STACK_NAME}
else
    set +e
    aws cloudformation update-stack --capabilities ${CAPABILITIES} --stack-name ${PREFIX}${STACK_NAME} --template-body file://${TEMPLATE_FILE} ${PARAMETERS} ${TAGS_FILE} ${CUSTOM_PARAMS} > ${UPDATE_LOG} 2>&1
    updateCode=$?
    set -e
    if [ "${updateCode}" -eq "255" ] ; then
        cat ${UPDATE_LOG}
        grep "No updates are to be performed" ${UPDATE_LOG}
    else
        UPDATE_LOG=/tmp/update.cfm.log
        aws cloudformation wait stack-update-complete --stack-name ${PREFIX}${STACK_NAME}
    fi
fi