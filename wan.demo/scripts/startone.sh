#!/bin/bash

if [ ! -n "$GSHOME" ] ; then
    echo Environment variable GSHOME not defined.
    echo Please set GSHOME to the home directory of your GigaSpaces installation.
    exit 1
fi

export LOOKUPGROUPS="ONE"
export JAVA_OPTS="%JAVA_OPTS% -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4266"
export JAVA_OPTS="%JAVA_OPTS% -Dcom.gigaspaces.system.registryPort=10198"
export JAVA_OPTS="%JAVA_OPTS% -Dcom.gigaspaces.start.httpPort=9913"
$GSHOME/bin/gs-agent.sh gsa.gsm 1 gsa.gsc 2
