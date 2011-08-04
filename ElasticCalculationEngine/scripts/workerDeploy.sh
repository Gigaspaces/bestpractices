#!/bin/bash

# This script is designed to deploy the datagrid into an existing GSM.

export SCRIPTDIR=`dirname $0`
if [ ! -f "$SCRIPTDIR/deployPU.sh" ] ; then
    echo Cannot find the deployPU.sh script in the $SCRIPTDIR directory.
    exit 1
fi

$SCRIPTDIR/deployPU.sh ece-worker
exit 0

