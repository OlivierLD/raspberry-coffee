#!/bin/bash
echo -e "Usage is:"
echo -e "    ${0} -left:0 -right:4 -bottom:2 -claw:1"
echo -e "-----------------------------------------"
CP=./build/libs/I2C-SPI-1.0-all.jar
sudo java -cp ${CP} i2c.samples.MeArmPilotInteractiveDemo $*
