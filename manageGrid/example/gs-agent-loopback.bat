@set SCRIPTDIR=%~dp0

@call setExampleEnv-loopback.bat

cd %JSHOMEDIR%\bin

@call gs-agent.bat gsa.gsm 0 gsa.global.gsm 0 gsa.lus 1 gsa.global.lus 0 gsa.gsc 0 gsa.global.gsc 0
