@setlocal
set GROOVY_HOME=D:\groovy-2.4.6
set PATH=%GROOVY_HOME%\bin;%PATH%
cd src
::
set PI4J_HOME=D:\Cloud\pi4j.2015
::
set CP=..\..\I2C.SPI\classes
set CP=%CP%;%PI4J_HOME%\libs\pi4j-core.jar
::
groovy -cp %CP% main
@endlocal