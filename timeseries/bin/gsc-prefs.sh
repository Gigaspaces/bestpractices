#!/bin/bash

if [ "$1" = "v32" ]; then
shift
export OSBITS="v32"
export JAVA_HOME="/opt/fxall/jdk32"
fi

if [ "$1" = "vgc" ]; then
shift
export GC_OUTPUT_OPTIONS="-verbose:gc  -XX:+PrintGCDetails -XX:+PrintGCTimeStamps "
fi

#
# This script is a wrapper around the "gs" script, and provides the command line instruction
# to start the GigaSpaces Grid Service Container

services="com.gigaspaces.start.services=\"GSC\""

EXT_JAVA_OPTIONS="-Dcom.gs.zones=orange  "
export EXT_JAVA_OPTIONS

JAVA_OPTIONS="-server -Xmx512m -Xss128k -XX:PermSize=64m -XX:MaxPermSize=128m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing 
-XX:CMSIncrementalDutyCycleMin=10 -XX:CMSIncrementalDutyCycle=50 -XX:ParallelGCThreads=4 -XX:+CMSClassUnloadingEnabled -XX:+HeapDumpOnOutOfMemoryError -XX:+CMSPermGenSweepingEnabled -XX:+UseParNewGC -XX:MaxGCPauseMillis=1000 -XX:GCTimeRatio=4 ${EXT_JAVA_OPTIONS} -Dcom.gs.transport_protocol.lrmi.classloading=false "
export JAVA_OPTIONS


#`dirname $0`/gs.sh start $services $*
nohup `dirname $0`/gs.sh start $services $* >>`dirname $0`/../logs/gsc-prefs.log  2>&1 &

