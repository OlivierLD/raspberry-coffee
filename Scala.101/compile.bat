@setlocal
@echo off
set PI4J_HOME=D:\Cloud\pi4j.2013\libs
@echo Compiling
::
@start /w scalac -sourcepath src -cp ..\I2C\build\libs\I2C-1.0.jar -d build\classes src\listener\TemperaturePressure.scala
@start /w scalac -sourcepath src -cp %PI4J_HOME%\pi4j-core.jar -d classes src\serial\SerialPI4J.scala
@echo Now running
@start /w scala -cp classes;%PI4J_HOME%\pi4j-core.jar serial.SerialPI4J
@echo Done
@endlocal
