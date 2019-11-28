@echo off
@setlocal
set PROP_FILE_NAME=nmea.mux.no.gps.yaml
set CP=..\build\libs\RESTNavServer-1.0-all.jar
:: Add the java-librxtx
set RXTX_HOME=C:\Users\olivier\rxtx-2.1-7-bins-r2
::
set CP=%CP%;%RXTX_HOME%\RXTXcomm.jar
::
set JAVA_OPTS=
set JAVA_OPTS=%JAVA_OPTS% -DdeltaT=69.2201
set JAVA_OPTS=%JAVA_OPTS% -Xms64M -Xmx1G
set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path=%RXTX_HOME%\Windows\i386-mingw32
set JAVA_OPTS=%JAVA_OPTS% -Dmux.properties=%PROP_FILE_NAME%
set COMMAND=java -cp %CP% %JAVA_OPTS% navrest.NavServer
echo "Running %COMMAND%"
%COMMAND%
::
echo "Bye now ✋"
::
@endlocal
