#!/bin/bash
PI4J_HOME=/home/pi/pi4j.2013/pi4j-distribution/target/distro-contents
CP=./classes
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
JAVA_OPTIONS=""
JAVA_OPTIONS="-Dserial.port=/dev/ttyACM0"
sudo java ${JAVA_OPTIONS} -cp ${CP} arduino.raspberrypi.SerialReaderWriter
