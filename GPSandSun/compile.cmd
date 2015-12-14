@setlocal
@echo off
set JAVA_HOME=D:\Java\jdk1.7.0_45
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAC_OTPIONS=-sourcepath .\src 
set JAVAC_OPTIONS=%JAVAC_OPTIONS% -d .\classes 
set CP=.\classes
set PI4J_HOME=D:\Cloud\pi4j
set CP=%CP%;%PI4J_HOME%\libs\pi4j-core.jar
:: set CP=%CP%;%PI4J_HOME%\libs\pi4j-device.jar
:: set CP=%CP%;%PI4J_HOME%\libs\pi4j-gpio-extension.jar
:: set CP=%CP%;%PI4J_HOME%\libs\pi4j-service.jar
set CP=%CP%;.\lib\almanactools.jar
set CP=%CP%;.\lib\geomutil.jar
set CP=%CP%;.\lib\nauticalalmanac.jar
set CP=%CP%;.\lib\nmeaparser.jar
set JAVAC_OPTIONS=%JAVAC_OPTIONS% -cp %CP%
echo Compiling
javac %JAVAC_OPTIONS% .\src\nmea\*.java .\src\readserialport\*.java
echo Done
@endlocal
