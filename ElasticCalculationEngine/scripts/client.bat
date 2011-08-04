@ECHO OFF

echo %CD%
echo %~dp0

if not "%GS_HOME%" == "" goto :gsdefined
echo Please define the GS_HOME environment variable.
goto :exit

:gsdefined
set GSCLASSPATH=%GSHOME%\lib\required\*
set BASECLASSPATH=%~dp0..\ece-client\target\ece-client-1.0.jar
echo %BASECLASSPATH% 
java -cp "%GSCLASSPATH%;%BASECLASSPATH%;%~dp0..\lib\*" org.openspaces.ece.client.ConsoleClient %*

:exit
