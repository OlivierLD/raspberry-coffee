#!/bin/bash
if [ "$PI4J_HOME" = "" ]
then
  PI4J_HOME=/opt/pi4j
fi
echo Compile, step 1
scalac -sourcepath src -cp ../../../I2C.SPI/build/libs/I2C.SPI-1.0.jar -d ../../build/classes/main ../../src/scala/listener/TemperaturePressure.scala
echo Compile, step 2
scalac -sourcepath src -cp $PI4J_HOME/lib/pi4j-core.jar -d ../../build/classes/main ../../src/scala/serial/SerialPI4J.scala
#
echo Running...
sudo scala -cp ../../build/classes/main:$PI4J_HOME/lib/pi4j-core.jar serial.SerialPI4J
echo Done!
