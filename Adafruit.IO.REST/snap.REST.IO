#!/bin/bash
PI4J_HOME=/opt/pi4j
# CP=./build/classes/main
CP=./build/libs/Adafruit.IO.REST-1.0.jar
CP=$CP:../RasPISamples/build/libs/RasPISamples-1.0-all.jar
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
#
sudo java -Dkey=$1 -cp $CP adafruit.io.sample.POSTSnapshot

