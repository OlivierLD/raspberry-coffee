@echo off
@setlocal
::
set RXTX_HOME=C:\OlivWork\git\oliv-soft-project-builder\olivsoft\release\all-3rd-party\rxtx.distrib
::
set CP=.\build\libs\SerialRxTx-1.0.jar
set CP=%CP%;%RXTX_HOME%\RXTXcomm.jar
::
set JAVA_OPT=
set JAVA_OPT="%JAVA_OPT% -Djava.library.path=%RXTX_HOME%\win64"
set JAVA_OPT="%JAVA_OPT% -Dserial.port=COM17"
set JAVA_OPT="%JAVA_OPT% -Dbaud.rate=115200"
java %JAVA_OPT% -cp %CP% console.SerialConsoleCLI
@endlocal
