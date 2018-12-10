#!/usr/bin/env bash
export AWS_DEFAULT_REGION=$1
set -x
set -e

STACK_NAME=${2}
TEMPLATE_FILE=$3

if [ "$4" != "" ]; then
    PARAMETERS="--parameters file://${4}"
else
    PARAMETERS=""
fi

PREFIX=$5

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
        found=1
    fi
    set -e

    return ${found};
}

UPDATE_LOG=/tmp/cf-update-${STACK_NAME}.log

if ! stackExists ${PREFIX}${STACK_NAME}; then
    aws cloudformation create-stack --capabilities CAPABILITY_NAMED_IAM --stack-name ${PREFIX}${STACK_NAME} --template-body file://${TEMPLATE_FILE} ${PARAMETERS}
    aws cloudformation wait stack-create-complete --stack-name ${PREFIX}${STACK_NAME}
else
    set +e
    aws cloudformation update-stack --capabilities CAPABILITY_NAMED_IAM --stack-name ${PREFIX}${STACK_NAME} --template-body file://${TEMPLATE_FILE} ${PARAMETERS} > ${UPDATE_LOG} 2>&1
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