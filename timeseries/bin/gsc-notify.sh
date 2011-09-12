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

EXT_JAVA_OPTIONS="-Dcom.gs.zones=indigo  "
export EXT_JAVA_OPTIONS

JAVA_OPTIONS="-server -showversion  -Xmx512m -Xss128k -XX:NewSize=64m -XX:MaxNewSize=256m -XX:PermSize=64m -XX:MaxPermSize=128m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:+CMSClassUnloadingEnabled -XX:+HeapDumpOnOutOfMemoryError -XX:+CMSPermGenSweepingEnabled -XX:+UseParNewGC -XX:+DisableExplicitGC ${EXT_JAVA_OPTIONS} "


export JAVA_OPTIONS

export VERBOSE=true
#`dirname $0`/gs.sh start $services $*
nohup `dirname $0`/gs.sh start $services $* >>`dirname $0`/../logs/gsc-notify.log  2>&1 &

