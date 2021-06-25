#!/bin/bash
echo Blinking 8 leds
CP=./classes:$PI4J_HOME/lib/pi4j-core.jar
sudo java -cp $CP gpio01.GPIO08led
