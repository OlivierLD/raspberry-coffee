@setlocal
@echo off
set CP=.\classes;D:\Cloud\pi4j.2013\libs\pi4j-core.jar
:: set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-device.jar
:: set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-example.jar
:: set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-gpio-extension.jar
:: set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-service.jar
:: set CP=%CP%;D:\OlivSoft.git\raspberry-coffee\ADC\lib\jansi-1.9.jar
set CP=%CP%;D:\_mywork\dev-corner\olivsoft\all-3rd-party\rxtx.distrib\RXTXcomm.jar
set CP=%CP%;D:\OlivSoft.git\raspberry-coffee\Serial.IO\classes
set CP=%CP%;D:\OlivSoft.git\raspberry-coffee\ADC\lib\Java-WebSocket-1.3.0.jar
set CP=%CP%;D:\OlivSoft.git\raspberry-coffee\RasPISamples\lib\json.jar
::
set OPT=-Djava.library.path=D:\_mywork\dev-corner\olivsoft\all-3rd-party\rxtx.distrib\win64 
java -cp %CP% %OPT% mindwave.samples.io.ws.WebSocketFeeder %*
@endlocal