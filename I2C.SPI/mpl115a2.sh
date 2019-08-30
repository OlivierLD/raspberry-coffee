#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
JAVA_OPTIONS=""
JAVA_OPTIONS="-Dmpl115a2.verbose=true"
sudo java $JAVA_OPTIONS -cp $CP i2c.sensor.MPL115A2
