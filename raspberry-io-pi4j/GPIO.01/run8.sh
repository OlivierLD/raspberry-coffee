#!/bin/bash
echo Blinking 8 leds
# CP=./classes:${PI4J_HOME}/lib/pi4j-core.jar
CP=./build/libs/GPIO.01-1.0-all.jar
sudo java -cp ${CP} gpio01.GPIO08led
