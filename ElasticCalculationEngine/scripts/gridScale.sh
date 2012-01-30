#!/bin/bash

# turn off wildcards
set -f

# make sure GS_HOME is defined
if [ ! -n "$GS_HOME" ] ; then
    echo Environment variable GS_HOME not defined.
    echo Please set GS_HOME to the home directory of your GigaSpaces installation.
    exit 1
fi

# establish where the script is running from, so we can use relative paths
SCRIPTDIR=`dirname $0`

# detect Cygwin
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true;
esac


export BASECLASSPATH=$SCRIPTDIR/../ece-scalingmodule/target/ece-scalingmodule-1.0.jar
export GSCLASSPATH="$GS_HOME/lib/required/"
export SEPARATOR=":"
if $cygwin; then
   export SEPARATOR=";"
   export GSCLASSPATH=`cygpath --windows $GSCLASSPATH`
   export BASECLASSPATH=`cygpath --windows $BASECLASSPATH`
fi

java -cp "$GSCLASSPATH*$SEPARATOR$BASECLASSPATH$SEPARATOR$SCRIPTDIR/../lib/*" org.openspaces.ece.scalingmodule.ScaleDataGrid $@

