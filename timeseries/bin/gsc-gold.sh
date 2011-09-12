#!/bin/bash

if [ "$1" = "vgc" ]; then
shift
export GC_OUTPUT_OPTIONS="-verbose:gc  -XX:+PrintGCDetails -XX:+PrintGCTimeStamps "
fi

#
# This script is a wrapper around the "gs" script, and provides the command line instruction
# to start the GigaSpaces Grid Service Container

services="com.gigaspaces.start.services=\"GSC\""

EXT_JAVA_OPTIONS="-Dcom.gs.zones=gold  "
export EXT_JAVA_OPTIONS

#JAVA_OPTIONS="-server -showversion -Xmx2048m -Xms1024m -XgcPrio:deterministic -XpauseTarget=30ms  ${EXT_JAVA_OPTIONS}"
#JAVA_OPTIONS="-server -showversion -Xmx8192m -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC  -XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=1000 -XX:+G1ParallelRSetUpdatingEnabled -XX:+G1ParallelRSetScanningEnabled  ${EXT_JAVA_OPTIONS}"

JAVA_OPTIONS="-server -showversion -Xmx4096m -Xss128k -XX:PermSize=32m -XX:MaxPermSize=128m -XX:CMSInitiatingOccupancyFraction=40 -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:CMSIncrementalDutyCycleMin=10 -XX:CMSIncrementalDutyCycle=50 -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:MaxGCPauseMillis=1000 -XX:GCTimeRatio=4 -XX:+ExplicitGCInvokesConcurrent -XX:+UseCompressedOops ${GC_OUTPUT_OPTIONS} ${EXT_JAVA_OPTIONS} "

export JAVA_OPTIONS

export VERBOSE=true
#`dirname $0`/gs.sh start $services $*
nohup `dirname $0`/gs.sh start $services $* >>`dirname $0`/../logs/gsc.log  2>&1 &

