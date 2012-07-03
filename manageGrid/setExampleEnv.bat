set JAVA_HOME=C:\DevTools\Java\jdk1.6.0_30
set JSHOMEDIR=C:\DevTools\GigaSpaces\gigaspaces-xap-premium-9.0.0-ga
set GS_HOME=%JSHOMEDIR%

set LOGDIR=logs
set PATH=%JAVA_HOME%\bin;%JSHOMEDIR%\bin;%PATH%

set NIC_ADDR=127.0.0.1

set LOGGING_PARMS=-Dcom.gigaspaces.logger.RollingFileHandler.filename-pattern=%LOGDIR%\{date,yyyy-MM-dd~HH.mm}-{service}-{pid}.log

set COMMON_JAVA_OPTIONS=-Dcom.gs.multicast.enabled=false ^
-XX:MaxPermSize=128m %LOGGING_PARMS%
 
set JAVA_OPTIONS=-Xmx512M %COMMON_JAVA_OPTIONS%
