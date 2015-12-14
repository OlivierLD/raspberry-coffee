#!/bin/bash
PI4J_HOME=/opt/pi4j
CP=./classes
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
JAVA_OPTIONS=""
JAVA_OPTIONS="-Dserial.port=/dev/ttyACM0"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=true"
sudo java $JAVA_OPTIONS -cp $CP arduino.raspberrypi.SerialLevelReader 2> err.txt
