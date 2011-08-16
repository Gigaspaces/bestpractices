#!/bin/sh
. ../../../SetEnv.sh
BASE=`dirname $0`
BASE=`pwd $BASE`

# Filter out the GIGASPACES_HOME from the prefs and calculate the location
cat .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs | grep -v GIGASPACES_HOME > prefs
echo org.eclipse.jdt.core.classpathVariable.GIGASPACES_HOME=$BASE/../../../GigaSpaces >> prefs
mv prefs .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs

# For the Ant-build scripts we move the GIGASPACES_TRAINING_HOME one level deeper as they work from a different context
GIGASPACES_TRAINING_HOME=../../../../

../../../Eclipse/Linux/eclipse/eclipse -data "$BASE"
        