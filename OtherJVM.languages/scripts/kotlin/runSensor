#!/bin/bash
#
# Reads a sensor from Kotlin
#
PI4J_HOME=/opt/pi4j
CP=../../../I2C.SPI/build/classes/main/
CP=$CP:../../src/kotlin/sensors.jar
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
#
# echo $CP
#
sudo java -cp $CP KotlinSensorsKt
