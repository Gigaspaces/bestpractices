@Echo off
@Call ..\..\..\SetEnv.cmd
SET BASE=%~dp0

rem Filter out the GIGASPACES_HOME from the prefs and calculate the location
type .metadata\.plugins\org.eclipse.core.runtime\.settings\org.eclipse.jdt.core.prefs | ..\..\..\software\utils\grep -v GIGASPACES_HOME > prefs
echo org.eclipse.jdt.core.classpathVariable.GIGASPACES_HOME=%BASE%/../../../GigaSpaces | ..\..\..\software\utils\sed s/\\/\//g >> prefs
move prefs .metadata\.plugins\org.eclipse.core.runtime\.settings\org.eclipse.jdt.core.prefs

rem For the Ant-build scripts we move the GIGASPACES_TRAINING_HOME one level deeper as they work from a different context
SET GIGASPACES_TRAINING_HOME=..\..\..\..\

START ..\..\..\Eclipse\Windows\eclipse\eclipse.exe -data "%~dp0"
        