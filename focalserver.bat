@echo off
@rem This script starts the JMX FocalServer

rem Use local variables
setlocal

@call "%~dp0\setenv.bat"

set CLASSPATH=%SPRING_JARS%;%GS_JARS%
%JAVACMD% %GS_LOGGING_CONFIG_FILE_PROP% -cp %CLASSPATH% -Djava.security.policy=%POLICY% com.gigaspaces.jmx.focalserver.FocalServer "config/tools/focalserver.xml"

endlocal
