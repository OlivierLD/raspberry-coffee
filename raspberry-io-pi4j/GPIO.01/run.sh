#!/bin/bash
echo Blinking a led, pin 01
# CP=./classes:${PI4J_HOME}/lib/pi4j-core.jar
CP=./build/libs/GPIO.01-1.0-all.jar
java -cp ${CP} gpio01.GPIO01led
