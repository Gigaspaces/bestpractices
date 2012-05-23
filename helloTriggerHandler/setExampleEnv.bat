set JAVA_HOME=C:\DevTools\Java\jdk1.6.0_30
set JSHOMEDIR=C:\DevTools\GigaSpaces\gigaspaces-xap-premium-9.0.0-ga
set ANT_HOME=C:\DevTools\apache-ant-1.8.1\

set GS_HOME=%JSHOMEDIR%

set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%JSHOMEDIR%\bin;%PATH%

set LOOKUPGROUPS=
set NIC_ADDR=localhost
set LOOKUPLOCATORS=%NIC_ADDR%

set JAVA_OPTIONS=-Djava.rmi.server.hostname=localhost

set SPACE_URL=jini://*/*/processorSpace

call %GS_HOME%\bin\setenv.bat
