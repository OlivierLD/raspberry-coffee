#!/bin/bash
#
# Reads a BME280 from Groovy
#
PI4J_HOME=/opt/pi4j
cd src/groovy
CP=${PI4J_HOME}/lib/pi4j-core.jar
CP=${CP}:../../../I2C.SPI/build/classes/main
#
# OPTS=-Dbme280.debug=true
#
sudo groovy -cp ${CP} mainRPi
