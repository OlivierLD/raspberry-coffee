#!/bin/bash
# Triple axis compass
#
CP=./build/libs/I2C.SPI-1.0-all.jar
#
JAVA_OPTIONS=""
JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose.raw=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose.mag=true"
#
sudo java ${JAVA_OPTIONS} -cp ${CP} i2c.sensor.HMC5883L
