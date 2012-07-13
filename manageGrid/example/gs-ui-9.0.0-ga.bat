@set SCRIPTDIR=%~dp0

@call setExampleEnv.bat

set JAVA_OPTIONS=-Xms2048m -Xmx2048m
@call %JSHOMEDIR%\bin\gs-ui.bat
