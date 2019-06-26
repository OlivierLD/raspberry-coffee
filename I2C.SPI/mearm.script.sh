#!/bin/bash
CP=./build/libs/I2C.SPI-1.0.jar
# CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
echo MeArm driven by a script
JAVA_OPTIONS=-Dscript.name=script.01.mearm
sudo java -cp $CP $JAVA_OPTIONS i2c.samples.MeArmScriptDemo $*
