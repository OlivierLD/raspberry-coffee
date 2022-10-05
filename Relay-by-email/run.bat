@echo off
@setlocal
set JDEV_HOME=D:\Oracle\Middleware\11.1.1.7
set CP=.\classes
set CP=%CP%;%JDEV_HOME%\oracle_common\modules\javax.mail.jar
set CP=%CP%;..\lib\json.jar
::
:: -Dverbose=true
java -classpath %CP% relay.email.SampleMain %*
@endlocal
