@set SCRIPTDIR=%~dp0

@call setDevEnv-9.0.0-ga.bat

set JAVA_OPTIONS=-Xms2048m -Xmx2048m
@call %JSHOMEDIR%\bin\gs-ui.bat
