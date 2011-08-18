

set LOOKUPGROUPS="ONE"
set JAVA_OPTS="%JAVA_OPTS% -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4266"
set JAVA_OPTS="%JAVA_OPTS% -Dcom.gigaspaces.system.registryPort=10198"
set JAVA_OPTS="%JAVA_OPTS% -Dcom.gigaspaces.start.httpPort=9913"
%GIGASPACES_HOME%/bin/gs-agent.bat gsa.gsm 1 gsa.gsc 2
