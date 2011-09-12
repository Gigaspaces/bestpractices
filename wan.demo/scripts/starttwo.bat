set LOOKUPGROUPS="TWO"

set JAVA_OPTS="%JAVA_OPTS% -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4166"
set JAVA_OPTS="%JAVA_OPTS% -Dcom.gigaspaces.system.registryPort=10098"
set JAVA_OPTS="%JAVA_OPTS% -Dcom.gigaspaces.start.httpPort=9813"
%GSHOME%/bin/gs-agent.bat gsa.gsm 1 gsa.gsc 2
