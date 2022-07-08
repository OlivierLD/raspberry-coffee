@setlocal
@echo off
::
set CP=.\classes
set CP=%CP%;D:\_mywork\dev-corner\olivsoft\all-3rd-party\rxtx.distrib\RXTXcomm.jar
set CP=%CP%;D:\OlivSoft.git\raspberry-coffee\Serial.IO\classes
::
set JAVA_OPT=-Djava.library.path=D:\_mywork\dev-corner\olivsoft\all-3rd-party\rxtx.distrib\win64 
:: set JAVA_OPT=%JAVA_OPT% -Dreplay.serial=serial.log
:: set JAVA_OPT=%JAVA_OPT% -Dreplay.serial=reworked.serial.log
:: set JAVA_OPT=%JAVA_OPT% -Dmindwave.verbose=true
java -cp %CP% %JAVA_OPT%  mindwave.samples.io.gui.MindWaves 
@endlocal