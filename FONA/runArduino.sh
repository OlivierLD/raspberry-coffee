#!/bin/bash
#
# For the RasPi connected to an Arduino
#
if [ "${PI4J_HOME}" = "" ]
then
  PI4J_HOME=/opt/pi4j
fi
#
CP=./classes
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dbaud.rate=115200"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dserial.port=/dev/ttyACM0"
#
sudo java ${JAVA_OPTIONS} -cp ${CP} fona.arduino.sample.SampleClient
