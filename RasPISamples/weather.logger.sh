#!/bin/bash
# PI4J_HOME=/home/pi/pi4j/pi4j-distribution/target/distro-contents
CP=./build/libs/RasPISamples-1.0-all.jar
#
sudo java -cp $CP raspisamples.log.net.WeatherDataLogging $*
