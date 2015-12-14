@echo off
@setlocal
set JDEV_HOME=D:\Oracle\Middleware\11.1.1.7
set CP=.\classes
set CP=%CP%;%JDEV_HOME%\oracle_common\modules\javax.mail.jar
set CP=%CP%;D:\Cloud\WebSocket.fallback\src\main\javax\lib\json.jar
::
:: -Dverbose=true
:: java -classpath %CP% pi4j.email.SampleMain %*
java -classpath %CP% pi4j.email.SampleMain %*
@endlocal
