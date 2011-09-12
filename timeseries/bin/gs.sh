#!/bin/bash
#
# This script provides the command and control utility for the
# GigaSpaces Technologies Inc. Service Grid
scriptDir="$(dirname $0)"
command_line=$*
start=

if [ "$1" = "start" ]; then
    start=1
    shift
    command_line=$*
fi

# Check to see if path conversion is needed
toNative() {
    # Check for Cygwin
    case $OS in
        Windows*)
           toWindows "$@";;
           *) echo $* ;;
    esac
}

# The call to setenv.sh can be commented out if necessary.
. `dirname $0`/setenv.sh

# set bootclasspath
bootclasspath="-Xbootclasspath/p:$(toNative $XML_JARS)"

GS_LIB="$JSHOMEDIR/lib"

# Function to find a file
getPathForFile() {
    filename="$1"
    if [ -f "$GS_LIB/platform/boot/$filename" ] ; then
	located="$GS_LIB/platform/boot/$filename"
    else
    echo "Cannot locate $filename in the expected directory structure, exiting"
    exit 1
    fi
}

# Locate the boot strapping jars
getPathForFile gs-boot.jar
gsboot=$located

cygwin=
case $OS in
    Windows*)
        cygwin=1
esac

# Cygwin utility to convert path if running under windows
toWindows() {
    cygpath -pw "$@"
}

# If the command is to start the Service Grid, invoke the SystemBoot facility.
# Otherwise invoke the CLI to interafce with the product
if [ "$start" = "1" ]; then
    NATIVE_DIR="$(toNative $GS_LIB/platform/native)"
    # Check for running on OS/X
    opSys=`uname -s`
    if [ $opSys = "Darwin" ] ; then
        export DYLD_LIBRARY_PATH=$NATIVE_DIR
    else
        if [ "$cygwin" = "1" ] ; then
            libpath="-Djava.library.path=$NATIVE_DIR"
        else
            if [ $OSBITS = "v32" ]; then
               export ORACLE_LIB=$ORACLE_HOME/lib32
            else
               export ORACLE_LIB=$ORACLE_HOME/lib
            fi 
            export LD_LIBRARY_PATH=$ORACLE_LIB:$NATIVE_DIR
        fi
    fi
# CPP Environment setup
    if [ -f "${JSHOMEDIR}/cpp/setenv.sh" ]; then
        . ${JSHOMEDIR}/cpp/setenv.sh
        libpath="-Djava.library.path=$CPP_SPACE_LIB_PATH"
	fi

    classpath="-cp $(toNative $PRE_CLASSPATH:$JDBC_JARS:$SIGAR_JARS:$JSHOMEDIR:$JMX_JARS:$gsboot:$POST_CLASSPATH)"
    launchTarget=com.gigaspaces.start.SystemBoot
 echo "classpath used: " $classpath

echo "LD_LIBRARY_PATH: " $LD_LIBRARY_PATH
echo "PATH: " $PATH
echo "ORACLE_LIB: " $ORACLE_LIB
echo "JAVA_OPTIONS" $JAVA_OPTIONS
    "$JAVACMD" ${JAVA_OPTIONS} -DagentId=${AGENT_ID} -DgsaServiceID=${GSA_SERVICE_ID} $bootclasspath $classpath ${RMI_OPTIONS} $libpath ${LOOKUP_GROUPS_PROP} ${LOOKUP_LOCATORS_PROP} -Dcom.gs.logging.debug=false ${GS_LOGGING_CONFIG_FILE_PROP} $NETWORK $DEBUG $launchTarget $command_line
else
    cliExt="config/tools/gs_cli.config"
    launchTarget=com.gigaspaces.admin.cli.GS
    classpath="-cp $(toNative $PRE_CLASSPATH:$JDBC_JARS:$JMX_JARS:$GS_JARS:$SPRING_JARS:$POST_CLASSPATH)"
 echo "classpath used: " $classpath
    "$JAVACMD" ${JAVA_OPTIONS} $bootclasspath $classpath ${RMI_OPTIONS} ${LOOKUP_GROUPS_PROP} ${LOOKUP_LOCATORS_PROP} -Dcom.gs.logging.debug=false ${GS_LOGGING_CONFIG_FILE_PROP} $launchTarget $cliExt $command_line
    
fi
