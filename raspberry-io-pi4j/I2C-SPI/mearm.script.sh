#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
# CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
echo MeArm driven by a script
echo -e "Usage is:"
echo -e "    ${0} -left:0 -right:4 -bottom:2 -claw:1"
echo -e "-----------------------------------------"
#
JAVA_OPTIONS=-Dscript.name=script.01.mearm
sudo java -cp ${CP} ${JAVA_OPTIONS} i2c.samples.MeArmPilotDemo $*
