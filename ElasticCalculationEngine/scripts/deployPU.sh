#!/bin/bash

# This script is designed to deploy the datagrid into an existing GSM.
if [ ! -n "$GS_HOME" ] ; then
    echo Environment variable GS_HOME not defined.
    echo Please set GS_HOME to the home directory of your GigaSpaces installation.
    exit 1
fi

export SCRIPTDIR=`dirname $0`

if [ "$1" = "-help" ] ; then
    echo Usage: $0 [-help|puDir]
    echo Deploys a processing unit to an existing XAP GSM.
    echo
    echo Options:
    echo -help   Displays this message.
    echo puDir   This will descend into $SCRIPTDIR/../puDir/target,
    echo         and deploy the processing unit that has the name matching
    echo         the puDir.
    echo
    echo Therefore, to deploy a processing unit called "myprocessingunit.jar":
    echo * The command is $0 myprocessingunit
    echo * The artifact to deployed is to be in the 
    echo   $SCRIPTDIR/myprocessingunit/target directory
    echo * The artifact is to be named myprocessingunit.jar
    echo    
    exit 0
fi

if [ "$1" = "" ]; then 
    echo No processing unit name supplied. See -help.
fi

if [ ! -f "$SCRIPTDIR/../$1/target/$1.jar" ] ; then
    echo "ElasticCalculationEngine project hasn't generated artifacts in expected locations."
    echo 
    echo "Please execute mvn package in the top-level directory and re-execute this script."
    exit 1
fi

$GS_HOME/bin/gs.sh deploy "$SCRIPTDIR/../$1/target/$1.jar"
exit 0

