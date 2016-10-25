@echo off
@setlocal
::
set RXTX_HOME=C:\OlivWork\git\oliv-soft-project-builder\olivsoft\release\all-3rd-party\rxtx.distrib
::
set CP=.\build\libs\SerialRxTx-1.0.jar
set CP=%CP%;%RXTX_HOME%\RXTXcomm.jar
::
java -Djava.library.path=%RXTX_HOME%\win64 -Dserial.port=COM17 -Dbaud.rate=115200 -cp %CP% sample.SerialEchoClient
@endlocal
