@echo off
@setlocal
::
:: Requires Java-LibRxTx to be installed. Google will tell you how, like this: http://rxtx.qbang.org/wiki/index.php/Download
:: >> Below, special care to take about RXTX_HOME and -Djava.library.path
::      RXTX_HOME is the fully qualified name of the folder where RXTXcomm.jar is
::      -Djava.library.path=%RXTX_HOME%\Windows\i368-mingw32 designates the folder where rxtxSerial.dll is
::
set PROP_FILE_NAME=nmea.mux.2.serial.yaml
::
set CP=.\build\libs\NMEA.multiplexer-1.0-all.jar
:: Add the java-librxtx
set RXTX_HOME=C:\Users\olivier\rxtx-2.1-7-bins-r2
::
set CP=%CP%;%RXTX_HOME%\RXTXcomm.jar
::
set JAVA_OPTS=
set JAVA_OPTS=%JAVA_OPTS% -DdeltaT=69.2201
set JAVA_OPTS=%JAVA_OPTS% -Xms64M -Xmx1G
set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path=%RXTX_HOME%\Windows\i368-mingw32
set JAVA_OPTS=%JAVA_OPTS% -Dmux.properties=%PROP_FILE_NAME%
set COMMAND=java -cp %CP% %JAVA_OPTS% nmea.mux.GenericNMEAMultiplexer
echo "Running %COMMAND%"
%COMMAND%
::
echo "Bye now ✋"
::
@endlocal
