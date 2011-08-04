#!/bin/bash

if [ ! -n "$GSHOME" ] ; then
    echo Environment variable GSHOME not defined.
    echo Please set GSHOME to the home directory of your GigaSpaces installation.
    exit 1
fi

export SCRIPTDIR=`dirname $0`
export CURDIR=`pwd`
pushd $GSHOME/bin
. ./setenv.sh
# set
export LOCAL_CLASSPATH=$CURDIR/../ece-client/target/ece-client-1.0.jar:~/.m2/repository/com/beust/jcommander/1.17/jcommander-1.17.jar
java -cp $GS_JARS:$LOCAL_CLASSPATH org.openspaces.ece.client.ConsoleClient -type executor
popd
