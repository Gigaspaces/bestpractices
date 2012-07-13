call ..\setExampleEnv.bat

call %GS_HOME%\bin\setenv.bat

%JAVA_HOME%\bin\java -Djava.rmi.server.hostname="%NIC_ADDR%" %LOGGING_PARMS% -cp ..\dist\grid-manager.jar;..\lib\jcommander-1.26.jar;\%GS_JARS% com.gigaspaces.adminutils.GridManager -l 127.0.0.1,192.168.56.1 -g gs --command startOne --hostAddress 127.0.0.1 -v
