#!/bin/bash

# The call to setenv.sh can be commented out if necessary.
export JSHOMEDIR=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

bootclasspath="-Xbootclasspath/p:$XML_JARS"

JAVACMD="${JAVA_HOME}/bin/java"

# Check for Cygwin
cygwin=
case $OS in
    Windows*)
        cygwin=1
esac
# For Cygwin, ensure paths are in UNIX format before anything is touched
if [ "$cygwin" = "1" ]; then
    CPS=";"
else
    CPS=":"
fi
export CPS

LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=${LOOKUPGROUPS}; export LOOKUP_GROUPS_PROP

if [ "${LOOKUPLOCATORS}" = "" ] ; then
LOOKUPLOCATORS=; export LOOKUPLOCATORS
fi
LOOKUP_LOCATORS_PROP="-Dcom.gs.jini_lus.locators=${LOOKUPLOCATORS}"; export LOOKUP_LOCATORS_PROP


for i in ${JSHOMEDIR}/lib/platform//velocity/*.jar
do
    VELOCITY_JARS=${VELOCITY_JARS}$CPS$i
done
export VELOCITY_JARS


COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} $bootclasspath -Dlb.vmDir="${JSHOMEDIR}/tools/apache" ${RMI_OPTIONS} ${LOOKUP_LOCATORS_PROP} ${LOOKUP_GROUPS_PROP} -Djava.security.policy=${POLICY} -Dcom.gs.home=${JSHOMEDIR} -classpath "${PRE_CLASSPATH}${CPS}${GS_JARS}${CPS}${SPRING_JARS}${CPS}${JDBC_JARS}${CPS}${VELOCITY_JARS}${CPS}${POST_CLASSPATH}" org.openspaces.pu.container.jee.lb.apache.ApacheLoadBalancerAgent $*"

echo
echo
echo Starting apache-lb-agent with line:
echo ${COMMAND_LINE}

#${COMMAND_LINE}
nohup ${COMMAND_LINE} >>${JSHOMEDIR}logs/apache-agent.log 2>&1 & 

echo
echo
